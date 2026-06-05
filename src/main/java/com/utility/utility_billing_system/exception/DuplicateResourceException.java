package com.utility.utility_billing_system.exception;

/**
 * Thrown when a create/update violates a uniqueness constraint
 * (e.g. duplicate email, national ID, or meter number). Handled as HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
