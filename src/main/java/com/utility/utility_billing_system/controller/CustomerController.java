package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.dto.customer.CustomerProfileRequest;
import com.utility.utility_billing_system.dto.customer.CustomerRequest;
import com.utility.utility_billing_system.dto.customer.CustomerResponse;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for customer profile management.
 * <p>
 * Customers manage their own profile; admins manage the customer registry.
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /** Returns the authenticated customer's own profile. Role: CUSTOMER. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get own customer profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile() {
        return ResponseEntity.ok(wrap(customerService.getMyProfile(), "Profile retrieved"));
    }

    /** Updates the authenticated customer's own profile. Role: CUSTOMER. */
    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update own customer profile")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateMyProfile(
            @Valid @RequestBody CustomerProfileRequest request) {
        return ResponseEntity.ok(wrap(customerService.updateMyProfile(request), "Profile updated"));
    }

    /** Lists all customers in the system. Role: ADMIN. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all customers (admin)")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        return ResponseEntity.ok(wrap(customerService.getAllCustomers(), "Customers retrieved"));
    }

    /** Retrieves a single customer by ID. Role: ADMIN. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get customer by ID (admin)")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(customerService.getCustomerById(id), "Customer retrieved"));
    }

    /** Creates a new customer profile. Role: ADMIN. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a customer profile (admin)")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(response, "Customer created"));
    }

    /** Updates an existing customer profile. Role: ADMIN. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update customer profile (admin)")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(wrap(customerService.updateCustomer(id, request), "Customer updated"));
    }

    /** Activates or deactivates a customer account. Role: ADMIN. */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update customer status (admin)")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateStatus(
            @PathVariable Long id, @RequestParam StatusType status) {
        return ResponseEntity.ok(wrap(customerService.updateStatus(id, status), "Customer status updated"));
    }

    /** Permanently removes a customer record. Role: ADMIN. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete customer (admin)")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(wrap(null, "Customer deleted"));
    }

    private <T> ApiResponse<T> wrap(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}
