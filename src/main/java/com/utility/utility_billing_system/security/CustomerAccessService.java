package com.utility.utility_billing_system.security;

import com.utility.utility_billing_system.entity.Bill;
import com.utility.utility_billing_system.entity.Payment;
import com.utility.utility_billing_system.exception.ResourceNotFoundException;
import com.utility.utility_billing_system.exception.UnauthorizedException;
import com.utility.utility_billing_system.repository.CustomerRepository;
import org.springframework.stereotype.Service;

/**
 * Enforces that customers can only access their own billing data.
 * Staff roles (admin, finance) bypass ownership checks.
 */
@Service
public class CustomerAccessService {

    private final CustomerRepository customerRepository;

    public CustomerAccessService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /** Returns the customer ID linked to the current user, or throws if none exists. */
    public Long requireCurrentCustomerId() {
        Long userId = SecurityUtils.getCurrentUserDetails().getUser().getId();
        return customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for current user"))
                .getId();
    }

    /** Ensures the current customer user owns the given customer record. */
    public void assertOwnCustomer(Long customerId) {
        if (isStaff()) {
            return;
        }
        if (!requireCurrentCustomerId().equals(customerId)) {
            throw new UnauthorizedException("You can only access your own customer data");
        }
    }

    /** Ensures the current customer user owns the given bill. */
    public void assertOwnBill(Bill bill) {
        if (isStaff()) {
            return;
        }
        assertOwnCustomer(bill.getCustomer().getId());
    }

    /** Ensures the current customer user owns the given payment (via its bill). */
    public void assertOwnPayment(Payment payment) {
        if (isStaff()) {
            return;
        }
        assertOwnBill(payment.getBill());
    }

    private boolean isStaff() {
        return SecurityUtils.getCurrentUserDetails().getAuthorities().stream()
                .anyMatch(a -> {
                    String role = a.getAuthority();
                    return "ROLE_ADMIN".equals(role)
                            || "ROLE_FINANCE".equals(role);
                });
    }
}
