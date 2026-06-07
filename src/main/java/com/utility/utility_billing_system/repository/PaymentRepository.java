package com.utility.utility_billing_system.repository;

import com.utility.utility_billing_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for {@link com.utility.utility_billing_system.entity.Payment} entities.
 * <p>
 * Lists all payments recorded against a given bill or customer.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBillId(Long billId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.bill b ORDER BY p.paymentDate DESC")
    List<Payment> findAllOrderByPaymentDateDesc();

    @Query("SELECT p FROM Payment p JOIN FETCH p.bill b WHERE b.customer.id = :customerId ORDER BY p.paymentDate DESC")
    List<Payment> findByCustomerIdOrderByPaymentDateDesc(@Param("customerId") Long customerId);
}
