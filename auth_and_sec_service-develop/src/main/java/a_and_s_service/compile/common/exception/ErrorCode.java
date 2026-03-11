package a_and_s_service.compile.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    SUCCESS(1000, "Success", HttpStatus.OK),

    // error for auth service
    INVALID_INFO(403, "Sai thông tin đăng nhập", HttpStatus.BAD_REQUEST),
    INVALID_USER_ID(404, "Thông tin user không hợp lệ", HttpStatus.BAD_REQUEST),

    // custom handling error for user behavior for 4xx
    VALIDATION_ERROR(4000, "Validation failed", HttpStatus.BAD_REQUEST),
    MALFORMED_JSON(4001, "Malformed JSON request", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(4002, "Invalid input", HttpStatus.BAD_REQUEST),

    UNAUTHENTICATED(4010, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    FORBIDDEN_ACTION(4030, "Forbidden", HttpStatus.FORBIDDEN),

    RESOURCE_NOT_FOUND(4040, "Not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST(4008, "Invalid request", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESOURCE(4090, "Conflict, resource already exists", HttpStatus.CONFLICT),

    UNSUPPORTED_MEDIA_TYPE(4150, "Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    TOO_MANY_REQUESTS(4290, "Too many requests", HttpStatus.TOO_MANY_REQUESTS),

    REQUEST_FAILED(4999, "Request failed", HttpStatus.BAD_REQUEST),
    UNEXPECTED_ERROR(5000, "Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR),
    PERMISSION_IN_USE(40901, "Permission in use", HttpStatus.CONFLICT),

    MONITOR_ALREADY_EXISTS(4091, "Hệ thống đã có tài khoản quản trị viên!", HttpStatus.CONFLICT),
    PHONE_NUMBER_EXISTS(4092, "Số điện thoại này đã được sử dụng!", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(4041, "Quyền hạn (Role) không tồn tại trong hệ thống!", HttpStatus.NOT_FOUND),

    EMAIL_SEND_FAILED (5001, "Failed to send OTP email", HttpStatus.INTERNAL_SERVER_ERROR),

    OTP_EXPIRED(4003, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_INVALID(4004, "Invalid OTP", HttpStatus.BAD_REQUEST),

    VERIFY_TOKEN_EXPIRED(4005, "Verify token has expired", HttpStatus.BAD_REQUEST),
    VERIFY_TOKEN_INVALID(4006, "Invalid verify token", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String defaultMessage;
    private final HttpStatus status;
}
