package fsoft.franchise.enums;

/**
 * Aggregate lifecycle status of a PaymentEntity (the intent record).
 * Transitions:
 * PENDING → PAID (gateway confirms SUCCESS transaction)
 * PENDING → FAILED (gateway confirms FAILED transaction)
 * PAID → REFUNDED (a REFUND transaction completes successfully)
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
