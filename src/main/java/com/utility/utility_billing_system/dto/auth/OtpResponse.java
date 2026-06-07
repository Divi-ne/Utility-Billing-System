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
    /** Present when {@code app.otp.delivery-mode=console} — use this code to verify your account. */
    private String otp;
}
