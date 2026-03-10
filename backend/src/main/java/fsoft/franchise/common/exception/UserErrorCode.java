package fsoft.franchise.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * User domain specific error codes.
 *
 * Error Code Range: 10xxx
 * - 100xx: User registration errors
 * - 101xx: User authentication errors
 * - 102xx: User profile errors
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // 100xx - Registration Errors
    USER_ALREADY_EXISTS(10001, "User already exists with this email", HttpStatus.CONFLICT, "user.already_exists"),
    INVALID_EMAIL_FORMAT(10002, "Invalid email format", HttpStatus.BAD_REQUEST, "user.invalid_email"),
    WEAK_PASSWORD(10003, "Password does not meet security requirements", HttpStatus.BAD_REQUEST, "user.weak_password"),
    USERNAME_TAKEN(10004, "Username is already taken", HttpStatus.CONFLICT, "user.username_taken"),

    // 101xx - Authentication Errors
    INVALID_CREDENTIALS(10101, "Invalid email or password", HttpStatus.UNAUTHORIZED, "user.invalid_credentials"),
    ACCOUNT_LOCKED(10102, "Account has been locked due to multiple failed attempts", HttpStatus.FORBIDDEN,
            "user.account_locked"),
    ACCOUNT_DISABLED(10103, "Account has been disabled", HttpStatus.FORBIDDEN, "user.account_disabled"),
    TOKEN_EXPIRED(10104, "Authentication token has expired", HttpStatus.UNAUTHORIZED, "user.token_expired"),
    TOKEN_INVALID(10105, "Invalid authentication token", HttpStatus.UNAUTHORIZED, "user.token_invalid"),

    // 102xx - Profile Errors
    USER_NOT_FOUND(10201, "User not found", HttpStatus.NOT_FOUND, "user.not_found"),
    PROFILE_UPDATE_FAILED(10202, "Failed to update user profile", HttpStatus.INTERNAL_SERVER_ERROR,
            "user.profile_update_failed"),
    AVATAR_UPLOAD_FAILED(10203, "Failed to upload avatar", HttpStatus.INTERNAL_SERVER_ERROR,
            "user.avatar_upload_failed");

    private final int code;
    private final String message;
    private final HttpStatus status;
    private final String errorKey;

    private static final String DOMAIN = "USER";

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    /**
     * Find UserErrorCode by code value.
     */
    public static UserErrorCode valueOf(int code) {
        for (UserErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("No matching UserErrorCode for [" + code + "]");
    }
}
