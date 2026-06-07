package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.Meter} entities.
 * <p>
 * Provides meter number uniqueness checks and customer-scoped meter listing.
 */
public interface MeterRepository extends JpaRepository<Meter, Long> {

    Optional<Meter> findByMeterNumber(String meterNumber);

    boolean existsByMeterNumber(String meterNumber);

    List<Meter> findByCustomerId(Long customerId);
}
