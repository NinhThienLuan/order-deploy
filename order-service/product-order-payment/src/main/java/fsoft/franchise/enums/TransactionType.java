package fsoft.franchise.enums;

/**
 * Direction of a real money-flow event recorded in TransactionEntity.
 * PAYMENT — money moved from customer to merchant
 * REFUND — money returned from merchant to customer
 * TOPUP — wallet/credit top-up (future use)
 */
public enum TransactionType {
    PAYMENT,
    REFUND,
    TOPUP
}
