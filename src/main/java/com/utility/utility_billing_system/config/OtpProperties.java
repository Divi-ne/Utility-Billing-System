package com.utility.utility_billing_system.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OTP generation and validation settings.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {

    /** Number of digits in the OTP code. */
    private int length = 6;

    /** Minutes before an OTP expires. */
    private int expirationMinutes = 10;

    /** Log OTP to console when email delivery is unavailable (dev only). */
    private boolean logToConsole = true;

    /** When false, signup skips email confirmation and login does not require OTP. */
    private boolean enabled = true;
}
