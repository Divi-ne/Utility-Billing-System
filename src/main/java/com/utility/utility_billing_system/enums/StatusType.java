package com.utility.utility_billing_system.enums;

/**
 * Generic active/inactive status used across users, customers, meters, and tariffs.
 */
public enum StatusType {
    /** Entity is operational and usable in business workflows. */
    ACTIVE,
    /** Entity is disabled; excluded from billing and login. */
    INACTIVE
}
