package com.utility.utility_billing_system.dto.tariff;

import com.utility.utility_billing_system.enums.MeterType;
import com.utility.utility_billing_system.enums.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing a tariff with full pricing configuration and tiers.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffResponse {

    private Long id;
    private Integer version;
    private MeterType meterType;
    private BigDecimal flatRate;
    private BigDecimal fixedServiceCharge;
    private BigDecimal vatPercentage;
    private BigDecimal latePaymentPenaltyPercentage;
    private LocalDate effectiveFrom;
    private StatusType status;
    private List<TariffTierResponse> tiers;
    private LocalDateTime createdAt;
}
