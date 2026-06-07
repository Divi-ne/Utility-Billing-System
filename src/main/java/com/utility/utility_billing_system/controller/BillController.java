package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.dto.bill.BillGenerateRequest;
import com.utility.utility_billing_system.dto.bill.BillResponse;
import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.enums.BillStatus;
import com.utility.utility_billing_system.service.BillService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for utility bill lifecycle management.
 * <p>
 * Admin and finance staff manage bills; customers view their own bills only.
 */
@RestController
@RequestMapping("/api/bills")
@Tag(name = "Bills", description = "Bill management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    /** Lists all bills in the system. Roles: ADMIN, FINANCE. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Get all bills")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAllBills() {
        return ResponseEntity.ok(wrap(billService.getAllBills(), "Bills retrieved"));
    }

    /** Retrieves a bill by ID. Roles: ADMIN, FINANCE, CUSTOMER. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get bill by ID")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(billService.getBillById(id), "Bill retrieved"));
    }

    /** Lists all bills for a specific customer. Roles: ADMIN, FINANCE, CUSTOMER. */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get bills by customer")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getBillsByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(wrap(billService.getBillsByCustomer(customerId), "Bills retrieved"));
    }

    /** Generates a monthly bill from a meter reading and active tariff. Role: ADMIN. */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate a monthly bill from meter reading")
    public ResponseEntity<ApiResponse<BillResponse>> generateBill(@Valid @RequestBody BillGenerateRequest request) {
        BillResponse response = billService.generateBill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(response, "Bill generated"));
    }

    /** Approves a pending bill and notifies the customer. Roles: ADMIN, FINANCE. */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Approve a pending bill")
    public ResponseEntity<ApiResponse<BillResponse>> approveBill(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(billService.approveBill(id), "Bill approved"));
    }

    /** Manually updates bill status (e.g. cancel). Roles: ADMIN, FINANCE. */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Update bill status")
    public ResponseEntity<ApiResponse<BillResponse>> updateStatus(
            @PathVariable Long id, @RequestParam BillStatus status) {
        return ResponseEntity.ok(wrap(billService.updateBillStatus(id, status), "Bill status updated"));
    }

    /** Applies a late-payment penalty to an overdue bill. Roles: ADMIN, FINANCE. */
    @PostMapping("/{id}/penalty")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @Operation(summary = "Apply late payment penalty to bill")
    public ResponseEntity<ApiResponse<Void>> applyPenalty(@PathVariable Long id) {
        billService.applyPenalty(id);
        return ResponseEntity.ok(wrap(null, "Penalty applied"));
    }

    private <T> ApiResponse<T> wrap(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}
