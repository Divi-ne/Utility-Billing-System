package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.Tariff;
import com.utility.utility_billing_system.enums.MeterType;
import com.utility.utility_billing_system.enums.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.Tariff} entities.
 * <p>
 * Supports versioned tariff lookup by meter type and effective date for bill generation.
 */
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    List<Tariff> findByMeterTypeAndStatus(MeterType meterType, StatusType status);

    Optional<Tariff> findTopByMeterTypeAndStatusAndEffectiveFromLessThanEqualOrderByVersionDesc(
            MeterType meterType, StatusType status, LocalDate billingDate);

    Optional<Tariff> findTopByMeterTypeAndStatusOrderByVersionDesc(MeterType meterType, StatusType status);
}
