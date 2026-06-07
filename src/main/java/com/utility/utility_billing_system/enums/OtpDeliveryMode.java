package com.utility.utility_billing_system.enums;

/**
 * How OTP codes are delivered to the user.
 */
public enum OtpDeliveryMode {
    /** Send OTP via configured SMTP (requires valid mail credentials). */
    EMAIL,
    /** Log OTP to the server console and return it in the API response (local development). */
    CONSOLE
}
