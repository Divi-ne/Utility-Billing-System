-- =============================================================================
-- V2: PostgreSQL Triggers
-- Automates bill generation and payment status updates at the database level
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Trigger 1: Auto-generate bill when a meter reading is inserted
-- Fires AFTER INSERT on meter_readings
-- Skips if customer/meter inactive, bill already exists, or no tariff found
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_generate_bill_on_reading()
RETURNS TRIGGER AS $$
DECLARE
    v_customer_status VARCHAR(20);
    v_meter_status    VARCHAR(20);
    v_meter_type      VARCHAR(20);
    v_customer_id     BIGINT;
    v_tariff_id       BIGINT;
    v_flat_rate       NUMERIC(12,4);
    v_service_charge  NUMERIC(12,2);
    v_vat_pct         NUMERIC(5,2);
    v_consumption     NUMERIC(12,2);
    v_consumption_amt NUMERIC(12,2);
    v_subtotal        NUMERIC(12,2);
    v_vat_amount      NUMERIC(12,2);
    v_total_amount    NUMERIC(12,2);
    v_billing_date    DATE;
BEGIN
    -- Load customer and meter status for business rule checks
    SELECT c.status, c.id, m.status, m.meter_type
    INTO v_customer_status, v_customer_id, v_meter_status, v_meter_type
    FROM meters m
    JOIN customers c ON c.id = m.customer_id
    WHERE m.id = NEW.meter_id;

    -- Inactive customers or meters cannot receive bills
    IF v_customer_status != 'ACTIVE' OR v_meter_status != 'ACTIVE' THEN
        RETURN NEW;
    END IF;

    -- Prevent duplicate bills for the same meter and billing period
    IF EXISTS (
        SELECT 1 FROM bills
        WHERE meter_id = NEW.meter_id
          AND billing_month = NEW.reading_month
          AND billing_year = NEW.reading_year
    ) THEN
        RETURN NEW;
    END IF;

    v_billing_date := make_date(NEW.reading_year, NEW.reading_month, 1);

    -- Use the latest active tariff effective on or before the billing date
    SELECT t.id, t.flat_rate, t.fixed_service_charge, t.vat_percentage
    INTO v_tariff_id, v_flat_rate, v_service_charge, v_vat_pct
    FROM tariffs t
    WHERE t.meter_type = v_meter_type
      AND t.status = 'ACTIVE'
      AND t.effective_from <= v_billing_date
    ORDER BY t.version DESC
    LIMIT 1;

    IF v_tariff_id IS NULL THEN
        RETURN NEW;
    END IF;

    v_consumption := NEW.current_reading - NEW.previous_reading;

    -- Flat rate or tier-based consumption charge
    IF v_flat_rate IS NOT NULL AND v_flat_rate > 0 THEN
        v_consumption_amt := ROUND(v_consumption * v_flat_rate, 2);
    ELSE
        SELECT COALESCE(SUM(
            LEAST(
                v_consumption,
                COALESCE(tt.max_consumption, v_consumption) - tt.min_consumption + 1
            ) * tt.rate
        ), 0)
        INTO v_consumption_amt
        FROM tariff_tiers tt
        WHERE tt.tariff_id = v_tariff_id;
        v_consumption_amt := ROUND(v_consumption_amt, 2);
    END IF;

    v_subtotal := v_consumption_amt + v_service_charge;
    v_vat_amount := ROUND(v_subtotal * v_vat_pct / 100, 2);
    v_total_amount := v_subtotal + v_vat_amount;

    INSERT INTO bills (
        customer_id, meter_id, tariff_id,
        billing_month, billing_year,
        consumption, flat_amount, tier_amount,
        service_charge, vat_amount, penalty_amount,
        total_amount, paid_amount, outstanding_balance,
        status, due_date, created_at, updated_at
    ) VALUES (
        v_customer_id, NEW.meter_id, v_tariff_id,
        NEW.reading_month, NEW.reading_year,
        v_consumption,
        CASE WHEN v_flat_rate IS NOT NULL AND v_flat_rate > 0 THEN v_consumption_amt ELSE 0 END,
        CASE WHEN v_flat_rate IS NULL OR v_flat_rate = 0 THEN v_consumption_amt ELSE 0 END,
        v_service_charge, v_vat_amount, 0,
        v_total_amount, 0, v_total_amount,
        'PENDING', v_billing_date + INTERVAL '30 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_generate_bill_on_reading
    AFTER INSERT ON meter_readings
    FOR EACH ROW
    EXECUTE FUNCTION fn_generate_bill_on_reading();

-- -----------------------------------------------------------------------------
-- Trigger 2: Update bill status when a payment is recorded
-- Fires AFTER INSERT on payments
-- Sets PAID when outstanding = 0; sends notification to linked customer user
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_update_bill_on_payment()
RETURNS TRIGGER AS $$
DECLARE
    v_total_paid       NUMERIC(12,2);
    v_total_amount     NUMERIC(12,2);
    v_outstanding      NUMERIC(12,2);
    v_customer_user_id BIGINT;
BEGIN
    -- Sum all payments for this bill
    SELECT COALESCE(SUM(amount), 0)
    INTO v_total_paid
    FROM payments
    WHERE bill_id = NEW.bill_id;

    SELECT total_amount
    INTO v_total_amount
    FROM bills
    WHERE id = NEW.bill_id;

    v_outstanding := v_total_amount - v_total_paid;
    IF v_outstanding < 0 THEN
        v_outstanding := 0;
    END IF;

    -- Update bill balances and status
    UPDATE bills
    SET paid_amount = v_total_paid,
        outstanding_balance = v_outstanding,
        status = CASE
            WHEN v_outstanding = 0 THEN 'PAID'
            WHEN v_total_paid > 0 THEN 'PARTIALLY_PAID'
            ELSE status
        END,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.bill_id;

    -- Notify customer when bill is fully paid
    IF v_outstanding = 0 THEN
        SELECT c.user_id INTO v_customer_user_id
        FROM bills b
        JOIN customers c ON c.id = b.customer_id
        WHERE b.id = NEW.bill_id;

        IF v_customer_user_id IS NOT NULL THEN
            INSERT INTO notifications (user_id, title, message, type, is_read, created_at)
            VALUES (
                v_customer_user_id,
                'Bill Fully Paid',
                'Your bill #' || NEW.bill_id || ' has been fully paid. Thank you!',
                'PAYMENT_RECEIVED',
                FALSE,
                CURRENT_TIMESTAMP
            );
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_bill_on_payment
    AFTER INSERT ON payments
    FOR EACH ROW
    EXECUTE FUNCTION fn_update_bill_on_payment();
