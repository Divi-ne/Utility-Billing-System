package com.utility.utility_billing_system.dto.tariff;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request body for a single consumption tier within a tiered tariff.
 */
@Getter
@Setter
public class TariffTierRequest {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal minConsumption;

    private BigDecimal maxConsumption;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal rate;
}
