package com.utility.utility_billing_system.entity;

import com.utility.utility_billing_system.enums.MeterType;
import com.utility.utility_billing_system.enums.StatusType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
 * JPA entity representing a versioned pricing schedule for a meter type.
 * <p>
 * Supports flat-rate or tiered consumption pricing plus fixed service charge,
 * VAT percentage, and late-payment penalty rate. Versioned to preserve billing history.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tariffs")
public class Tariff extends BaseEntity {

    /** Incrementing version number per meter type. */
    @Column(nullable = false)
    private Integer version;

    /** WATER or ELECTRICITY — determines which meters use this tariff. */
    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 20)
    private MeterType meterType;

    /** Per-unit flat rate; null when tiered pricing is used. */
    @Column(name = "flat_rate", precision = 12, scale = 4)
    private BigDecimal flatRate;

    @Column(name = "fixed_service_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal fixedServiceCharge;

    @Column(name = "vat_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatPercentage;

    @Column(name = "late_payment_penalty_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal latePaymentPenaltyPercentage;

    /** Date from which this tariff version applies. */
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusType status = StatusType.ACTIVE;

    /** Tiered rate brackets when flat rate is not set. */
    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<TariffTier> tiers = new ArrayList<>();
}
