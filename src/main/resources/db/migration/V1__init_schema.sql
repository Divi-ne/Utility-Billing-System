-- =============================================================================
-- V1: Initial Database Schema
-- Creates all core tables, indexes, and default security roles
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Security: roles and user accounts
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(30)  NOT NULL UNIQUE,  -- ROLE_ADMIN, ROLE_OPERATOR, etc.
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,  -- removed in V3; email becomes login
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,       -- BCrypt-hashed
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE or INACTIVE
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

-- Many-to-many: one user can have multiple roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- -----------------------------------------------------------------------------
-- Customer billing profiles (linked to a user account optionally)
-- -----------------------------------------------------------------------------
CREATE TABLE customers (
    id           BIGSERIAL PRIMARY KEY,
    full_name    VARCHAR(150) NOT NULL,
    national_id  VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20)  NOT NULL,
    address      VARCHAR(255) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    user_id      BIGINT REFERENCES users(id),  -- links to customer login account
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- Meters: one customer can own multiple meters
-- -----------------------------------------------------------------------------
CREATE TABLE meters (
    id                BIGSERIAL PRIMARY KEY,
    meter_number      VARCHAR(50)  NOT NULL UNIQUE,
    meter_type        VARCHAR(20)  NOT NULL,   -- WATER or ELECTRICITY
    installation_date DATE         NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    customer_id       BIGINT       NOT NULL REFERENCES customers(id),
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- Monthly meter readings (one per meter per month/year)
-- -----------------------------------------------------------------------------
CREATE TABLE meter_readings (
    id                BIGSERIAL PRIMARY KEY,
    meter_id          BIGINT        NOT NULL REFERENCES meters(id),
    previous_reading  NUMERIC(12,2) NOT NULL,
    current_reading   NUMERIC(12,2) NOT NULL,
    reading_date      DATE          NOT NULL,
    reading_month     INTEGER       NOT NULL,
    reading_year      INTEGER       NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT uq_meter_reading_period UNIQUE (meter_id, reading_month, reading_year),
    CONSTRAINT chk_reading_order CHECK (current_reading > previous_reading)
);

-- -----------------------------------------------------------------------------
-- Tariff pricing (versioned; new versions only affect future bills)
-- -----------------------------------------------------------------------------
CREATE TABLE tariffs (
    id                              BIGSERIAL PRIMARY KEY,
    version                         INTEGER       NOT NULL,
    meter_type                      VARCHAR(20)   NOT NULL,
    flat_rate                       NUMERIC(12,4),              -- optional flat rate
    fixed_service_charge            NUMERIC(12,2) NOT NULL,
    vat_percentage                  NUMERIC(5,2)  NOT NULL,
    late_payment_penalty_percentage NUMERIC(5,2)  NOT NULL,
    effective_from                  DATE          NOT NULL,     -- billing date must be >= this
    status                          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at                      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP
);

-- Tier-based rates (used when flat_rate is null)
CREATE TABLE tariff_tiers (
    id               BIGSERIAL PRIMARY KEY,
    tariff_id        BIGINT        NOT NULL REFERENCES tariffs(id) ON DELETE CASCADE,
    min_consumption  NUMERIC(12,2) NOT NULL,
    max_consumption  NUMERIC(12,2),             -- null = unlimited upper bound
    rate             NUMERIC(12,4) NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- Bills and payments
-- -----------------------------------------------------------------------------
CREATE TABLE bills (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT        NOT NULL REFERENCES customers(id),
    meter_id            BIGINT        NOT NULL REFERENCES meters(id),
    tariff_id           BIGINT        NOT NULL REFERENCES tariffs(id),
    billing_month       INTEGER       NOT NULL,
    billing_year        INTEGER       NOT NULL,
    consumption         NUMERIC(12,2) NOT NULL,
    flat_amount         NUMERIC(12,2),
    tier_amount         NUMERIC(12,2),
    service_charge      NUMERIC(12,2) NOT NULL,
    vat_amount          NUMERIC(12,2) NOT NULL,
    penalty_amount      NUMERIC(12,2) DEFAULT 0,
    total_amount        NUMERIC(12,2) NOT NULL,
    paid_amount         NUMERIC(12,2) DEFAULT 0,
    outstanding_balance NUMERIC(12,2) NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING',  -- PENDING, PAID, etc.
    due_date            DATE,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP,
    CONSTRAINT uq_bill_period UNIQUE (meter_id, billing_month, billing_year)
);

CREATE TABLE payments (
    id           BIGSERIAL PRIMARY KEY,
    bill_id      BIGINT        NOT NULL REFERENCES bills(id),
    amount       NUMERIC(12,2) NOT NULL,
    payment_date TIMESTAMP     NOT NULL,
    payment_type VARCHAR(20)   NOT NULL,   -- PARTIAL or FULL
    reference    VARCHAR(255),
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- In-app notifications stored per user
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    title      VARCHAR(150) NOT NULL,
    message    VARCHAR(500) NOT NULL,
    type       VARCHAR(30)  NOT NULL,   -- BILL_GENERATED, PAYMENT_RECEIVED, etc.
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- Performance indexes
-- -----------------------------------------------------------------------------
CREATE INDEX idx_customers_national_id ON customers(national_id);
CREATE INDEX idx_meters_customer_id ON meters(customer_id);
CREATE INDEX idx_meter_readings_meter_id ON meter_readings(meter_id);
CREATE INDEX idx_bills_customer_id ON bills(customer_id);
CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_payments_bill_id ON payments(bill_id);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_tariffs_meter_type_version ON tariffs(meter_type, version DESC);

-- -----------------------------------------------------------------------------
-- Default roles required by Spring Security
-- -----------------------------------------------------------------------------
INSERT INTO roles (name) VALUES
    ('ROLE_ADMIN'),
    ('ROLE_OPERATOR'),
    ('ROLE_FINANCE'),
    ('ROLE_CUSTOMER');
