package com.utility.utility_billing_system.dto.bill;

import com.utility.utility_billing_system.enums.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing a bill with full charge breakdown and payment status.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long meterId;
    private String meterNumber;
    private Long tariffId;
    private Integer tariffVersion;
    private Integer billingMonth;
    private Integer billingYear;
    private BigDecimal consumption;
    private BigDecimal flatAmount;
    private BigDecimal tierAmount;
    private BigDecimal serviceCharge;
    private BigDecimal vatAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingBalance;
    private BillStatus status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
