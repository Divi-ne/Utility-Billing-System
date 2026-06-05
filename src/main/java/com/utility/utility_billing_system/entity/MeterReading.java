package com.utility.utility_billing_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a periodic meter reading for a billing period.
 * <p>
 * One reading per meter per month. Consumption = currentReading - previousReading.
 * Bill generation requires a reading matching the billing month/year.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "meter_readings",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"meter_id", "reading_month", "reading_year"}
        )
)
public class MeterReading extends BaseEntity {

    /** Meter this reading belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    /** Meter value at start of the billing period. */
    @Column(name = "previous_reading", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousReading;

    /** Meter value at end of the billing period. */
    @Column(name = "current_reading", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentReading;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    /** Billing month (1-12) for this reading. */
    @Column(name = "reading_month", nullable = false)
    private Integer readingMonth;

    /** Billing year for this reading. */
    @Column(name = "reading_year", nullable = false)
    private Integer readingYear;
}
