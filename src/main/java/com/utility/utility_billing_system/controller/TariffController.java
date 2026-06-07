package com.utility.utility_billing_system.controller;

import com.utility.utility_billing_system.dto.common.ApiResponse;
import com.utility.utility_billing_system.dto.tariff.TariffRequest;
import com.utility.utility_billing_system.dto.tariff.TariffResponse;
import com.utility.utility_billing_system.dto.tariff.TariffUpdateRequest;
import com.utility.utility_billing_system.enums.MeterType;
import com.utility.utility_billing_system.service.TariffService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for tariff (pricing) configuration.
 * <p>
 * Admin staff create and update tariff versions.
 */
@RestController
@RequestMapping("/api/tariffs")
@Tag(name = "Tariffs", description = "Tariff management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    /** Lists all tariff versions. Role: ADMIN. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all tariffs")
    public ResponseEntity<ApiResponse<List<TariffResponse>>> getAllTariffs() {
        return ResponseEntity.ok(wrap(tariffService.getAllTariffs(), "Tariffs retrieved"));
    }

    /** Retrieves a tariff by ID. Role: ADMIN. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get tariff by ID")
    public ResponseEntity<ApiResponse<TariffResponse>> getTariffById(@PathVariable Long id) {
        return ResponseEntity.ok(wrap(tariffService.getTariffById(id), "Tariff retrieved"));
    }

    /** Lists active tariffs for a meter type (water/electricity). Role: ADMIN. */
    @GetMapping("/meter-type/{meterType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get active tariffs by meter type")
    public ResponseEntity<ApiResponse<List<TariffResponse>>> getByMeterType(@PathVariable MeterType meterType) {
        return ResponseEntity.ok(wrap(tariffService.getTariffsByMeterType(meterType), "Tariffs retrieved"));
    }

    /** Creates a new tariff version with optional tier rates. Role: ADMIN. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new tariff version")
    public ResponseEntity<ApiResponse<TariffResponse>> createTariff(@Valid @RequestBody TariffRequest request) {
        TariffResponse response = tariffService.createTariff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(wrap(response, "Tariff created"));
    }

    /** Updates an existing tariff's rates and tiers. Role: ADMIN. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing tariff")
    public ResponseEntity<ApiResponse<TariffResponse>> updateTariff(
            @PathVariable Long id, @Valid @RequestBody TariffUpdateRequest request) {
        return ResponseEntity.ok(wrap(tariffService.updateTariff(id, request), "Tariff updated"));
    }

    private <T> ApiResponse<T> wrap(T data, String message) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }
}
