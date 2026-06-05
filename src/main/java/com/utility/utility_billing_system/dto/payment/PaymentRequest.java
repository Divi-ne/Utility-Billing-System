package com.utility.utility_billing_system.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request body for processing a bill payment ({@code POST /api/payments}).
 * <p>
 * Amount must not exceed the bill's outstanding balance.
 */
@Getter
@Setter
public class PaymentRequest {

    @NotNull
    private Long billId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @Size(max = 255)
    private String reference;
}
