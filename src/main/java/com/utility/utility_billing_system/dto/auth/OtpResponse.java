package com.utility.utility_billing_system.dto.auth;

import lombok.Builder;
import lombok.Getter;

/**
 * Response after sending or verifying an OTP.
 */
@Getter
@Builder
public class OtpResponse {

    private String email;
    private String message;
    private int expirationMinutes;
}
