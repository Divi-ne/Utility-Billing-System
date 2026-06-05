package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.Bill;
import com.utility.utility_billing_system.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.Bill} entities.
 * <p>
 * Prevents duplicate bills per meter/month and supports customer and status filtering.
 */
public interface BillRepository extends JpaRepository<Bill, Long> {

    boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);

    Optional<Bill> findByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);

    List<Bill> findByCustomerId(Long customerId);

    List<Bill> findByStatus(BillStatus status);
}
