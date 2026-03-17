package fsoft.franchise.auth.module.controller;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;
import fsoft.franchise.auth.common.response.ApiResponse;
import fsoft.franchise.auth.common.security.JwtProperties;
import fsoft.franchise.auth.module.dto.account.request.ChangePasswordRequestDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForAdminDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForUserDTO;
import fsoft.franchise.auth.module.dto.login.request.LoginRequestDTO;
import fsoft.franchise.auth.module.dto.login.response.LoginResponseDTO;
import fsoft.franchise.auth.module.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "refresh_token";
    private static final String ACCESS_COOKIE = "access_token";

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            HttpServletRequest request,
            @RequestBody @Valid LoginRequestDTO body) {

        LoginResponseDTO tokens = authService.login(body);

        ResponseCookie atCookie = createCookie(ACCESS_COOKIE, tokens.accessToken(), "/", jwtProperties.accessTtlMs());
        ResponseCookie rtCookie = createCookie(REFRESH_COOKIE, tokens.refreshToken(), "/api/v1/auth", jwtProperties.refreshTtlMs());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, atCookie.toString())
                .header(HttpHeaders.SET_COOKIE, rtCookie.toString())
                .body(ApiResponse.<LoginResponseDTO>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Login success")
                        .result(tokens)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refresh(
            HttpServletRequest request,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }

        LoginResponseDTO tokens = authService.refresh(refreshToken);

        ResponseCookie atCookie = createCookie(ACCESS_COOKIE, tokens.accessToken(), "/", jwtProperties.accessTtlMs());
        ResponseCookie rtCookie = createCookie(REFRESH_COOKIE, tokens.refreshToken(), "/api/v1/auth", jwtProperties.refreshTtlMs());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, atCookie.toString())
                .header(HttpHeaders.SET_COOKIE, rtCookie.toString())
                .body(ApiResponse.<LoginResponseDTO>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Refresh success")
                        .result(tokens)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        ResponseCookie clearAt = createCookie(ACCESS_COOKIE, "", "/", 0);
        ResponseCookie clearRt = createCookie(REFRESH_COOKIE, "", "/api/v1/auth", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAt.toString())
                .header(HttpHeaders.SET_COOKIE, clearRt.toString())
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Logout success")
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(HttpServletRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        authService.logoutAll(email);

        ResponseCookie clearAt = createCookie(ACCESS_COOKIE, "", "/", 0);
        ResponseCookie clearRt = createCookie(REFRESH_COOKIE, "", "/api/v1/auth", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAt.toString())
                .header(HttpHeaders.SET_COOKIE, clearRt.toString())
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Logged out from all devices")
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    private ResponseCookie createCookie(String name, String value, String path, long maxAgeMs) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path(path)
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            HttpServletRequest request,
            @RequestBody @Valid ChangePasswordRequestDTO body) {
        authService.changePassword(body);
        ResponseCookie clearAt = createCookie(ACCESS_COOKIE, "", "/", 0);
        ResponseCookie clearRt = createCookie(REFRESH_COOKIE, "", "/api/v1/auth", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAt.toString())
                .header(HttpHeaders.SET_COOKIE, clearRt.toString())
                .body(ApiResponse.<Void>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("Password changed successfully. Please log in again.")
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build());
    }

    // Endpoint test bảo mật, chỉ có thể truy cập nếu token hợp lệ (test cách refresh api hoạt động)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponseForAdminDTO>> testSecure(HttpServletRequest request, @RequestParam("accessToken") String accessToken) {

        AccountResponseForAdminDTO result = authService.getCurrentUser(accessToken);

        return ResponseEntity.ok(ApiResponse.<AccountResponseForAdminDTO>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Token vẫn còn ngon!")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }
}