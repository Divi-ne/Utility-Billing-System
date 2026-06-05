package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.MeterReading} entities.
 * <p>
 * Enforces one reading per meter per month via existence checks.
 */
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    boolean existsByMeterIdAndReadingMonthAndReadingYear(Long meterId, Integer month, Integer year);

    Optional<MeterReading> findByMeterIdAndReadingMonthAndReadingYear(Long meterId, Integer month, Integer year);
}
