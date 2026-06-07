package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.dto.payment.PaymentRequest;
import com.utility.utility_billing_system.dto.payment.PaymentResponse;
import com.utility.utility_billing_system.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for bill payment processing.
 * <p>
 * Finance staff record payments; customers view their own payment history.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment processing endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Lists all payments in the system. Role: FINANCE. */
    @GetMapping
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Get all payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(wrap(paymentService.getAllPayments(), "Payments retrieved"));
    }

    /** Lists all payments for a customer. Roles: FINANCE, CUSTOMER (own data only). */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get payments by customer")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(wrap(paymentService.getPaymentsByCustomer(customerId), "Payments retrieved"));
    }

    /** Lists all payments applied to a bill. Roles: FINANCE, CUSTOMER (own data only). */
    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasAnyRole('FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get payments for a bill")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByBill(@PathVariable Long billId) {
        return ResponseEntity.ok(wrap(paymentService.getPaymentsByBill(billId), "Payments retrieved"));
    }

    /** Retrieves a single payment record by ID. Roles: FINANCE, CUSTOMER (own data only). */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(paymentService.getPaymentById(id), "Payment retrieved"));
    }

    /** Records a partial or full payment against a bill. Role: FINANCE. */
    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Process a partial or full payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(response, "Payment processed"));
    }

    private <T> ApiResponse<T> wrap(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}
