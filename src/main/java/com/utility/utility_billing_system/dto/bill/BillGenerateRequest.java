package com.utility.utility_billing_system.dto.bill;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for generating a bill from a meter reading ({@code POST /api/bills/generate}).
 * <p>
 * Billing month/year must match the referenced meter reading period.
 */
@Getter
@Setter
public class BillGenerateRequest {

    @NotNull
    private Long meterReadingId;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer billingMonth;

    @NotNull
    @Min(2000)
    private Integer billingYear;
}
