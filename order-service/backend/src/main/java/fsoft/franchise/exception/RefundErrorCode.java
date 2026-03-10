package fsoft.franchise.exception;

import fsoft.franchise.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Refund domain specific error codes.
 */
@Getter
@RequiredArgsConstructor
public enum RefundErrorCode implements ErrorCode {

    REFUND_NOT_FOUND(30001, "Refund not found", HttpStatus.NOT_FOUND, "refund.not_found"),
    REFUND_ALREADY_EXISTS(30002, "Refund request already exists for this order", HttpStatus.CONFLICT, "refund.already_exists"),
    INVALID_ORDER_STATUS_FOR_REFUND(30003, "Order status is not valid for refund", HttpStatus.BAD_REQUEST, "refund.invalid_order_status"),
    REFUND_ALREADY_PROCESSED(30004, "Refund has already been processed", HttpStatus.CONFLICT, "refund.already_processed"),
    PAYMENT_NOT_FOUND(30005, "Payment not found for this order", HttpStatus.NOT_FOUND, "refund.payment_not_found"),
    TRANSACTION_NOT_FOUND(30006, "Transaction not found for this payment", HttpStatus.NOT_FOUND, "refund.transaction_not_found"),
    INVALID_REFUND_AMOUNT(30007, "Refund amount must equal order total amount", HttpStatus.BAD_REQUEST, "refund.invalid_amount");

    private final int code;
    private final String message;
    private final HttpStatus status;
    private final String errorKey;

    @Override
    public String getDomain() {
        return "REFUND";
    }

    public static RefundErrorCode valueOf(int code) {
        for (RefundErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("No matching RefundErrorCode for [" + code + "]");
    }
}

