package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.dto.meter.MeterRequest;
import com.utility.utility_billing_system.dto.meter.MeterResponse;
import com.utility.utility_billing_system.enums.StatusType;
import com.utility.utility_billing_system.service.MeterService;
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
 * REST controller for utility meter registration and management.
 * <p>
 * Each customer may have one or more meters (water, electricity). Operators
 * install and maintain meters; customers can view meters on their account.
 */
@RestController
@RequestMapping("/api/meters")
@Tag(name = "Meters", description = "Meter management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class MeterController {

    private final MeterService meterService;

    public MeterController(MeterService meterService) {
        this.meterService = meterService;
    }

    /** Lists all meters. Roles: ADMIN, OPERATOR, FINANCE. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @Operation(summary = "Get all meters")
    public ResponseEntity<ApiResponse<List<MeterResponse>>> getAllMeters() {
        return ResponseEntity.ok(wrap(meterService.getAllMeters(), "Meters retrieved"));
    }

    /** Retrieves a meter by ID. Roles: ADMIN, OPERATOR, FINANCE, CUSTOMER. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get meter by ID")
    public ResponseEntity<ApiResponse<MeterResponse>> getMeterById(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(meterService.getMeterById(id), "Meter retrieved"));
    }

    /** Lists meters assigned to a customer. Roles: ADMIN, OPERATOR, FINANCE, CUSTOMER. */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE', 'CUSTOMER')")
    @Operation(summary = "Get meters by customer")
    public ResponseEntity<ApiResponse<List<MeterResponse>>> getMetersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(wrap(meterService.getMetersByCustomer(customerId), "Meters retrieved"));
    }

    /** Registers a new meter for a customer. Roles: ADMIN, OPERATOR. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Create a new meter")
    public ResponseEntity<ApiResponse<MeterResponse>> createMeter(@Valid @RequestBody MeterRequest request) {
        MeterResponse response = meterService.createMeter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(response, "Meter created"));
    }

    /** Updates meter details. Roles: ADMIN, OPERATOR. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Update meter")
    public ResponseEntity<ApiResponse<MeterResponse>> updateMeter(
            @PathVariable Long id, @Valid @RequestBody MeterRequest request) {
        return ResponseEntity.ok(wrap(meterService.updateMeter(id, request), "Meter updated"));
    }

    /** Activates or deactivates a meter. Roles: ADMIN, OPERATOR. */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Update meter status")
    public ResponseEntity<ApiResponse<MeterResponse>> updateStatus(
            @PathVariable Long id, @RequestParam StatusType status) {
        return ResponseEntity.ok(wrap(meterService.updateStatus(id, status), "Meter status updated"));
    }

    /** Removes a meter record. Role: ADMIN only. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete meter")
    public ResponseEntity<ApiResponse<Void>> deleteMeter(@PathVariable Long id) {
        meterService.deleteMeter(id);
        return ResponseEntity.ok(wrap(null, "Meter deleted"));
    }

    private <T> ApiResponse<T> wrap(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}
