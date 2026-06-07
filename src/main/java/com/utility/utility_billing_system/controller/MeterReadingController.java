package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.dto.reading.MeterReadingRequest;
import com.utility.utility_billing_system.dto.reading.MeterReadingResponse;
import com.utility.utility_billing_system.service.MeterReadingService;
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
 * REST controller for periodic meter reading records.
 * <p>
 * Operators capture monthly consumption readings.
 */
@RestController
@RequestMapping("/api/meter-readings")
@Tag(name = "Meter Readings", description = "Meter reading management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    public MeterReadingController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    /** Lists all meter readings. Role: OPERATOR. */
    @GetMapping
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Get all meter readings")
    public ResponseEntity<ApiResponse<List<MeterReadingResponse>>> getAllReadings() {
        return ResponseEntity.ok(wrap(meterReadingService.getAllReadings(), "Readings retrieved"));
    }

    /** Retrieves a meter reading by ID. Role: OPERATOR. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Get meter reading by ID")
    public ResponseEntity<ApiResponse<MeterReadingResponse>> getReadingById(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(meterReadingService.getReadingById(id), "Reading retrieved"));
    }

    /** Records a new monthly meter reading. Role: OPERATOR. */
    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Record a new meter reading")
    public ResponseEntity<ApiResponse<MeterReadingResponse>> createReading(
            @Valid @RequestBody MeterReadingRequest request) {
        MeterReadingResponse response = meterReadingService.createReading(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(response, "Reading recorded"));
    }

    private <T> ApiResponse<T> wrap(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}
