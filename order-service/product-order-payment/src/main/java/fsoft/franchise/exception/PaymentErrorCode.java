package fsoft.franchise.exception;

import fsoft.franchise.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Payment domain specific error codes.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_FAILED(30001, "Payment processing failed", HttpStatus.BAD_REQUEST, "payment.failed"),
    INSUFFICIENT_FUNDS(30002, "Insufficient funds for the transaction", HttpStatus.BAD_REQUEST,
            "payment.insufficient_funds"),
    TRANSACTION_NOT_FOUND(30003, "Transaction record not found", HttpStatus.NOT_FOUND, "payment.transaction_not_found"),
    INVALID_PAYMENT_METHOD(30004, "Invalid or unsupported payment method", HttpStatus.BAD_REQUEST,
            "payment.invalid_method"),
    PAYMENT_PROVIDER_ERROR(30005, "Error occurred while communicating with payment provider", HttpStatus.BAD_GATEWAY,
            "payment.provider_error"),
    PAYMENT_AMOUNT_MISMATCH(30006, "Payment amount does not match order total", HttpStatus.BAD_REQUEST,
            "payment.amount_mismatch"),
    PAYMENT_NOT_FOUND(404, "Payment not found", HttpStatus.NOT_FOUND, "payment.not_found"),
    PAYMENT_ACCESS_DENIED(403, "Payment access denied", HttpStatus.FORBIDDEN, "payment.access_denied"),
    PAYMENT_ALREADY_PAID(30007, "Order already has a completed payment", HttpStatus.CONFLICT, "payment.already_paid"),
    PAYMENT_PENDING_ACTIVE(30008, "An active payment is still in progress for this order. Please complete it or wait for it to expire before retrying.", HttpStatus.CONFLICT, "payment.pending_active");

    private final int code;
    private final String message;
    private final HttpStatus status;
    private final String errorKey;

    @Override
    public String getDomain() {
        return "PAYMENT";
    }

    public static PaymentErrorCode valueOf(int code) {
        for (PaymentErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("No matching PaymentErrorCode for [" + code + "]");
    }
}
