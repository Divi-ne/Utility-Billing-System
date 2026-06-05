package com.utility.utility_billing_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity representing a consumption tier within a tiered tariff.
 * <p>
 * Each tier defines a consumption range (min to max units) and a per-unit rate.
 * A null maxConsumption means unlimited upper bound (top tier).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tariff_tiers")
public class TariffTier extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    /** Lower bound of consumption units for this tier (inclusive). */
    @Column(name = "min_consumption", nullable = false, precision = 12, scale = 2)
    private BigDecimal minConsumption;

    /** Upper bound of consumption units; null for the unlimited top tier. */
    @Column(name = "max_consumption", precision = 12, scale = 2)
    private BigDecimal maxConsumption;

    /** Price per consumption unit within this tier. */
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal rate;
}
