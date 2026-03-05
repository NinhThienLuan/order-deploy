package fsoft.franchise.enums;

/**
 * Outcome of a single gateway-level money-flow event (TransactionEntity).

 * PENDING — awaiting gateway confirmation
 * SUCCESS — gateway confirmed money moved
 * FAILED — gateway rejected or timed out
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED
}
