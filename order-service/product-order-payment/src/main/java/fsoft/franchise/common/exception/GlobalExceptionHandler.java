package fsoft.franchise.common.exception;

import fsoft.franchise.exception.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import fsoft.franchise.common.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

@Slf4j // tự động log lỗi
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // Handle lỗi nghiệp vụ ở phía hệ thống throw ra
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Business error: method={} path={} code={} message={}",
            request.getMethod(), request.getRequestURI(), errorCode.getCode(), ex.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(buildError(errorCode, ex.getMessage(), request.getRequestURI(), null));

    }

    // Handle cho lỗi ở phía input param/path/query, (ConstraintViolationException),
    // trả 400 + map field -> message.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        log.warn("Constraint violation: method={} path={} errors={}",
            request.getMethod(), request.getRequestURI(), errors);

        ErrorCode ec = CommonErrorCode.VALIDATION_FAILED;
        return ResponseEntity.status(ec.getStatus())
                .body(buildError(ec, "Validation failed", request.getRequestURI(), errors));
    }

    // Handle cho lỗi ở phía input field trong @RequestBody, gom field và trả về 400
    // + errors
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("Method argument invalid: path={} errors={}", extractPath(request), errors);

        ErrorCode ec = CommonErrorCode.VALIDATION_FAILED;
        ApiResponse<Object> body = buildError(ec, "Validation failed", extractPath(request), errors);
        return new ResponseEntity<>(body, ec.getStatus());
    }

    // Handler JSON body sai format / không parse được
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        log.warn("Malformed request body: path={} reason={}", extractPath(request), ex.getMessage());

        ErrorCode ec = CommonErrorCode.INVALID_INPUT;
        ApiResponse<Object> body = buildError(ec, "Malformed JSON or invalid input", extractPath(request), null);
        return new ResponseEntity<>(body, ec.getStatus());
    }

    // Handle lỗi không đủ quyền (Spring Security), trả 403 FORBIDDEN.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex,
            HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication != null ? authentication.getName() : "anonymous";
        log.warn("Access denied: method={} path={} principal={} reason={}",
            request.getMethod(), request.getRequestURI(), principal, ex.getMessage());
        ErrorCode ec = CommonErrorCode.FORBIDDEN;
        return ResponseEntity.status(ec.getStatus())
                .body(buildError(ec, ec.getMessage(), request.getRequestURI(), null));
    }

    // Handle mọi lỗi không lường trước, log stacktrace và trả 500 UNEXPECTED_ERROR.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error:", ex);
        ErrorCode ec = CommonErrorCode.UNEXPECTED_ERROR;
        return ResponseEntity.status(ec.getStatus())
                .body(buildError(ec, ec.getMessage(), request.getRequestURI(), null));
    }

    // Helper dùng để dựng ApiRes lỗi
    private ApiResponse<Object> buildError(ErrorCode errorCode, String message, String path,
            Map<String, String> errors) {
        return ApiResponse.builder()
                .code(errorCode.getCode())
                .errorKey(errorCode.getErrorKey())
                .message(message)
                .result(null)
                .errors(errors)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    // Helper lấy path từ WebRequest (format "uri=/...") cho các override method.
    private String extractPath(WebRequest request) {
        String desc = request.getDescription(false);
        return desc.startsWith("uri=") ? desc.substring(4) : desc;
    }

//     private ErrorCode mapErrorCode(HttpStatus status) {
//     int s = status.value();
//
//     switch (s) {
//     case 400:
//     return CommonErrorCode.REQUEST_FAILED;
//     case 401:
//     return CommonErrorCode.UNAUTHENTICATED;
//     case 403:
//     return ErrorCode.FORBIDDEN_ACTION;
//     case 404:
//     return ErrorCode.RESOURCE_NOT_FOUND;
//     case 415:
//     return ErrorCode.UNSUPPORTED_MEDIA_TYPE;
//     case 429:
//     return ErrorCode.TOO_MANY_REQUESTS;
//     default:
//     if (status.is4xxClientError()) {
//     return ErrorCode.REQUEST_FAILED;
//     }
//     return ErrorCode.UNEXPECTED_ERROR;
//     }
//     }

}
