package com.utility.utility_billing_system.dto.payment;

import com.utility.utility_billing_system.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a recorded payment with remaining bill balance.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long billId;
    private BigDecimal amount;
    private PaymentType paymentType;
    private String reference;
    private LocalDateTime paymentDate;
    private BigDecimal remainingBalance;
}
