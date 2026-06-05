package com.utility.utility_billing_system.entity;

import com.utility.utility_billing_system.enums.MeterType;
import com.utility.utility_billing_system.enums.StatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a physical utility meter installed at a customer premises.
 * <p>
 * Each meter has a unique number, type (water/electricity), and monthly readings
 * that drive bill generation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meters")
public class Meter extends BaseEntity {

    /** Unique identifier printed on the physical meter. */
    @Column(name = "meter_number", nullable = false, unique = true, length = 50)
    private String meterNumber;

    /** Utility type: WATER or ELECTRICITY; determines applicable tariff. */
    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 20)
    private MeterType meterType;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    /** Only ACTIVE meters accept readings and generate bills. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusType status = StatusType.ACTIVE;

    /** Customer who owns this meter. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "meter", fetch = FetchType.LAZY)
    @Builder.Default
    private List<MeterReading> readings = new ArrayList<>();
}
