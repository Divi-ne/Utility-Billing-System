package com.utility.utility_billing_system.entity;

import com.utility.utility_billing_system.enums.BillStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a monthly utility bill for a customer meter.
 * <p>
 * Generated from a meter reading and effective tariff. Tracks charge breakdown,
 * payment progress, due date, and lifecycle status (PENDING → PAID/OVERDUE).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "bills",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"meter_id", "billing_month", "billing_year"}
        )
)
public class Bill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    /** Units consumed during the billing period. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumption;

    @Column(name = "flat_amount", precision = 12, scale = 2)
    private BigDecimal flatAmount;

    @Column(name = "tier_amount", precision = 12, scale = 2)
    private BigDecimal tierAmount;

    @Column(name = "service_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal serviceCharge;

    @Column(name = "vat_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "penalty_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    /** Total bill amount including VAT, service charge, and penalties. */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /** Remaining amount owed after partial payments. */
    @Column(name = "outstanding_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal outstandingBalance;

    /** Payment lifecycle: PENDING, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.PENDING;

    /** Payment deadline; bills past due may receive penalties. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    @OneToMany(mappedBy = "bill", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
}
