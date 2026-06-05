package com.utility.utility_billing_system.exception;

/**
 * Thrown when a requested entity (user, bill, meter, etc.) does not exist in the database.
 * Handled as HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
