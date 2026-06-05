package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.Customer} entities.
 * <p>
 * Supports lookup by national ID and by linked user ID for self-service profile access.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNationalId(String nationalId);

    boolean existsByNationalId(String nationalId);

    Optional<Customer> findByUserId(Long userId);
}
