package fsoft.franchise.controller;

import fsoft.franchise.common.response.ApiResponse;
import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.security.JwtProperties;
import fsoft.franchise.dto.auth.LoginRequest;
import fsoft.franchise.dto.auth.AccountResponse;
import fsoft.franchise.dto.auth.LoginResponse;
import fsoft.franchise.service.AccountsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

/**
 * Auth controller for the franchise/payment service.
 * Provides login, logout, and /me endpoints.
 *
 * - POST /api/v1/auth/login → authenticates and sets HttpOnly cookies
 * - POST /api/v1/auth/logout → clears the access_token cookie
 * - GET /api/v1/auth/me → returns current user profile (id, email, roles, etc.)
 *
 * The access_token is set as an HttpOnly cookie so it is automatically
 * sent with every subsequent request, enabling Postman / test clients
 * to call order and payment endpoints without manually managing headers.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication — login, logout, and current user profile")
public class AuthController {

        private static final String ACCESS_COOKIE = "access_token";
        private static final String REFRESH_COOKIE = "refresh_token";

        private final AccountsService accountsService;
        private final JwtProperties jwtProperties;

        // ──────────────────────────────────────────────────────────────
        // POST /api/auth/login
        // ──────────────────────────────────────────────────────────────
        @PostMapping("/login")
        @Operation(summary = "Login", description = "Authenticate with email and password. Returns access and refresh tokens as HttpOnly cookies.")
        public ResponseEntity<ApiResponse<LoginResponse>> login(
                        HttpServletRequest request,
                        @RequestBody @Valid LoginRequest body) {

                LoginResponse tokens = accountsService.login(body);

                ResponseCookie atCookie = createCookie(ACCESS_COOKIE, tokens.accessToken(), "/",
                                jwtProperties.accessTtlMs());
                ResponseCookie rtCookie = createCookie(REFRESH_COOKIE, tokens.refreshToken(), "/api/v1/auth",
                                jwtProperties.refreshTtlMs());

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, atCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, rtCookie.toString())
                                .body(ApiResponse.<LoginResponse>builder()
                                                .code(CommonErrorCode.SUCCESS.getCode())
                                                .message("Login successful")
                                                .result(tokens)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        // ──────────────────────────────────────────────────────────────
        // POST /api/auth/logout
        // ──────────────────────────────────────────────────────────────
        @PostMapping("/logout")
        @Operation(summary = "Logout", description = "Clears the access_token and refresh_token cookies.")
        public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
                accountsService.logout();

                // Clear both cookies by setting maxAge = 0
                ResponseCookie clearAt = createCookie(ACCESS_COOKIE, "", "/", 0);
                ResponseCookie clearRt = createCookie(REFRESH_COOKIE, "", "/api/v1/auth", 0);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, clearAt.toString())
                                .header(HttpHeaders.SET_COOKIE, clearRt.toString())
                                .body(ApiResponse.<Void>builder()
                                                .code(CommonErrorCode.SUCCESS.getCode())
                                                .message("Logout successful")
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        // ──────────────────────────────────────────────────────────────
        // GET /api/auth/me
        // Returns the full profile of the authenticated user.
        // Useful to get the account id / roles needed for testing
        // order and payment endpoints.
        // ──────────────────────────────────────────────────────────────
        @GetMapping("/me")
        @Operation(summary = "Get current user", description = "Returns the full profile of the authenticated user including roles and permissions.")
        public ResponseEntity<ApiResponse<AccountResponse>> getMe(HttpServletRequest request) {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                if (email == null || email.isBlank()) {
                        throw new ApiException(CommonErrorCode.UNAUTHORIZED);
                }

                AccountResponse profile = accountsService.getMe(email);

                return ResponseEntity.ok(
                                ApiResponse.<AccountResponse>builder()
                                                .code(CommonErrorCode.SUCCESS.getCode())
                                                .message("Account info retrieved successfully")
                                                .result(profile)
                                                .timestamp(Instant.now())
                                                .path(request.getRequestURI())
                                                .build());
        }

        // ──────────────────────────────────────────────────────────────
        // Helpers
        // ──────────────────────────────────────────────────────────────
        private ResponseCookie createCookie(String name, String value, String path, long maxAgeMs) {
                return ResponseCookie.from(name, value)
                                .httpOnly(true)
                                .secure(false) // set to true in production (HTTPS)
                                .sameSite("Lax")
                                .path(path)
                                .maxAge(Duration.ofMillis(maxAgeMs))
                                .build();
        }
}
