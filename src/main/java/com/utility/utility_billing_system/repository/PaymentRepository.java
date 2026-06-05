package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.Payment} entities.
 * <p>
 * Lists all payments recorded against a given bill.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBillId(Long billId);
}
