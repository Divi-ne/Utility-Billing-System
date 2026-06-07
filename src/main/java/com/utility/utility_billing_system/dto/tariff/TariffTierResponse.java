package com.utility.utility_billing_system.dto.tariff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Response DTO representing a consumption tier's range and rate.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffTierResponse {

    private Long id;
    private BigDecimal minConsumption;
    private BigDecimal maxConsumption;
    private BigDecimal rate;
}
