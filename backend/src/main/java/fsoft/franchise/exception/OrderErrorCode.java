package fsoft.franchise.exception;

import fsoft.franchise.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Order domain specific error codes.
 */
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(404, "Order not found", HttpStatus.NOT_FOUND, "order.not_found"),
    INVALID_ORDER_STATUS(409, "Invalid order status transition", HttpStatus.BAD_REQUEST, "order.invalid_status"),
    ORDER_NOT_OWNED(403, "User not owned this order", HttpStatus.FORBIDDEN, "order.not_owned"),
    ORDER_ALREADY_PAID(20003, "Order has already been paid", HttpStatus.CONFLICT, "order.already_paid"),
    ORDER_EXPIRED(20004, "Order has expired", HttpStatus.GONE, "order.expired"),
    PRODUCT_OUT_OF_STOCK(20005, "One or more products in the order are out of stock", HttpStatus.BAD_REQUEST,
            "order.out_of_stock");

    private final int code;
    private final String message;
    private final HttpStatus status;
    private final String errorKey;

    @Override
    public String getDomain() {
        return "ORDER";
    }

    public static OrderErrorCode valueOf(int code) {
        for (OrderErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("No matching OrderErrorCode for [" + code + "]");
    }
}
