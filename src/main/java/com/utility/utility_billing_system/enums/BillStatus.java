package com.utility.utility_billing_system.enums;

/**
 * Lifecycle status of a utility bill tracking payment progress.
 */
public enum BillStatus {
    /** Bill issued; awaiting finance approval before customer notification. */
    PENDING,
    /** Approved by finance; customer notified and payment can be recorded. */
    APPROVED,
    /** Some payment received; outstanding balance remains. */
    PARTIALLY_PAID,
    /** Fully paid; outstanding balance is zero. */
    PAID,
    /** Past due date; penalty may have been applied. */
    OVERDUE,
    /** Bill voided; no further payments accepted. */
    CANCELLED
}
