package com.utility.utility_billing_system.validation;

/**
 * Shared validation patterns for request DTOs.
 */
public final class ValidationPatterns {

    public static final String NATIONAL_ID = "^\\d{16}$";
    public static final String NATIONAL_ID_MESSAGE = "National ID must be exactly 16 digits";

    public static final String PHONE_NUMBER = "^\\d{10}$";
    public static final String PHONE_NUMBER_MESSAGE = "Phone number must be exactly 10 digits";

    private ValidationPatterns() {
    }
}
