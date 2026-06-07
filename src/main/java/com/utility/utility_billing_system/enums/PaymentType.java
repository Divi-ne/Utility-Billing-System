package com.utility.utility_billing_system.enums;

/**
 * Classification of a payment based on whether it settles the full bill.
 */
public enum PaymentType {
    /** Payment covers part of the outstanding balance. */
    PARTIAL,
    /** Payment settles the entire outstanding balance. */
    FULL
}
