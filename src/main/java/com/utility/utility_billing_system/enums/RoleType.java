package com.utility.utility_billing_system.enums;

/**
 * Security roles defining access levels in the utility billing system.
 * <p>
 * Prefixed with ROLE_ to align with Spring Security's hasRole() convention.
 */
public enum RoleType {
    /** Configure tariffs, approve bills, manage users. */
    ROLE_ADMIN,
    /** Capture meter readings. */
    ROLE_OPERATOR,
    /** Approve bills and process payments. */
    ROLE_FINANCE,
    /** View own bills and payment history. */
    ROLE_CUSTOMER
}
