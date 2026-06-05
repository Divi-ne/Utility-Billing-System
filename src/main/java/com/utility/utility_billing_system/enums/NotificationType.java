package com.utility.utility_billing_system.enums;

/**
 * Category of in-app notification for filtering and display styling.
 */
public enum NotificationType {
    /** A new monthly bill has been created. */
    BILL_GENERATED,
    /** A payment was recorded against a bill. */
    PAYMENT_RECEIVED,
    /** Reminder that a bill payment is due. */
    PAYMENT_REMINDER,
    /** Important account status change or alert. */
    ACCOUNT_ALERT,
    /** General system message. */
    SYSTEM
}
