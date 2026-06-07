package com.utility.utility_billing_system.exception;

/**
 * Thrown when authentication fails or the security context has no authenticated user.
 * Handled as HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
