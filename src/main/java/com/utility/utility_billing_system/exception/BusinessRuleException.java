package com.utility.utility_billing_system.exception;

/**
 * Thrown when an operation violates a business rule
 * (e.g. paying a cancelled bill, inactive meter reading). Handled as HTTP 400 Bad Request.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
