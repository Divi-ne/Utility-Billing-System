package com.utility.utility_billing_system.dto.auth;

import com.utility.utility_billing_system.enums.StatusType;
import lombok.Builder;
import lombok.Getter;

/**
 * Response after customer self-registration, before email confirmation.
 */
@Getter
@Builder
public class SignupResponse {

    private Long userId;
    private String fullName;
    private String email;
    private StatusType status;
    private boolean emailVerified;
    /** Present when OTP is enabled and delivery mode is CONSOLE. */
    private String otp;
    private Integer expirationMinutes;
}
