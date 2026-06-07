package com.utility.utility_billing_system.dto.tariff;

import com.utility.utility_billing_system.enums.StatusType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request body for updating an existing tariff ({@code PUT /api/tariffs/{id}}).
 */
@Getter
@Setter
public class TariffUpdateRequest {

    private BigDecimal flatRate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal fixedServiceCharge;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal vatPercentage;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal latePaymentPenaltyPercentage;

    @NotNull
    private LocalDate effectiveFrom;

    private StatusType status;

    @Valid
    private List<TariffTierRequest> tiers;
}
