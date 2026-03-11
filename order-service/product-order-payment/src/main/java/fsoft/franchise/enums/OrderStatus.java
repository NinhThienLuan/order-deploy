package fsoft.franchise.enums;

/**
 * Trạng thái đơn hàng theo schema DB: ORDERS.status (Paid, Preparing, Ready, Completed, Canceled).
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    PAID,
    PREPARING,
    READY,
    COMPLETED,
    CANCELED
}
