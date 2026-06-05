package com.utility.utility_billing_system.enums;

/**
 * Security roles defining access levels in the utility billing system.
 * <p>
 * Prefixed with ROLE_ to align with Spring Security's hasRole() convention.
 */
public enum RoleType {
    /** Full system access: user management, deletions, all operations. */
    ROLE_ADMIN,
    /** Field operations: meters, readings, customer management. */
    ROLE_OPERATOR,
    /** Financial operations: tariffs, bills, payments, penalties. */
    ROLE_FINANCE,
    /** Self-service portal: own profile, bills, meters, notifications. */
    ROLE_CUSTOMER
}
