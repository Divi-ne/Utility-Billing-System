package com.utility.utility_billing_system.dto.customer;

import com.utility.utility_billing_system.enums.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response DTO representing a customer profile with contact and status details.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private String fullName;
    private String nationalId;
    private String email;
    private String phoneNumber;
    private String address;
    private StatusType status;
    private Long userId;
    private LocalDateTime createdAt;
}
