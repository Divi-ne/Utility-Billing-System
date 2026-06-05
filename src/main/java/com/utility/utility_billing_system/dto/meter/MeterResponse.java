package com.utility.utility_billing_system.dto.meter;

import com.utility.utility_billing_system.enums.MeterType;
import com.utility.utility_billing_system.enums.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing a meter with customer assignment details.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterResponse {

    private Long id;
    private String meterNumber;
    private MeterType meterType;
    private LocalDate installationDate;
    private StatusType status;
    private Long customerId;
    private String customerName;
    private LocalDateTime createdAt;
}
