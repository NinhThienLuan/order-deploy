package a_and_s_service.compile.module.controller;

import a_and_s_service.compile.common.exception.ErrorCode;
import a_and_s_service.compile.common.response.ApiResponse;
import a_and_s_service.compile.module.dto.account.request.RegisterRequestDTO;
import a_and_s_service.compile.module.dto.account.request.AdminCreateAccountRequestDTO;
import a_and_s_service.compile.module.dto.account.response.AccountResponseForAdminDTO;
import a_and_s_service.compile.module.dto.account.response.AccountResponseForUserDTO;
import a_and_s_service.compile.module.dto.forget_password.request.VerifyOTPRequestDTO;
import a_and_s_service.compile.module.service.account.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

//    For admin

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('account:write')")
    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<AccountResponseForAdminDTO>> createUserForAdmin(
            HttpServletRequest request,
            @RequestBody @Valid AdminCreateAccountRequestDTO body) {

        AccountResponseForAdminDTO result = accountService.createUserForAdmin(body);

        return ResponseEntity.ok(
                ApiResponse.<AccountResponseForAdminDTO>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Account created successfully")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build()
        );

    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('account:write')")
    @PutMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<AccountResponseForAdminDTO>> updateUserForAdmin(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestBody @Valid AdminCreateAccountRequestDTO body) {

        AccountResponseForAdminDTO result = accountService.updateUserForAdmin(id, body);

        return ResponseEntity.ok(ApiResponse.<AccountResponseForAdminDTO>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Account updated by admin successfully")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }


//    For customer
    @PostMapping("/user/register")
    public ResponseEntity<ApiResponse<Void>> register(HttpServletRequest request, @RequestBody @Valid RegisterRequestDTO body) {

        accountService.initiateRegistration(body);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Registration initiated successfully. Please check your email for OTP.")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    @PostMapping("/user/register/verify-otp")
    public ResponseEntity<ApiResponse<AccountResponseForUserDTO>> verifyOtpAndRegister(HttpServletRequest request, @RequestBody @Valid VerifyOTPRequestDTO body) {
        AccountResponseForUserDTO result = accountService.verifyOtpAndRegister(body);

        return ResponseEntity.ok(
                ApiResponse.<AccountResponseForUserDTO>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("OTP verified and registration completed successfully.")
                        .result(result)
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @PostMapping("/user/register/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(HttpServletRequest request, @RequestParam String email) {
        accountService.resendOtp(email);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("OTP resent successfully. Please check your email.")
                        .timestamp(Instant.now())
                        .path(request.getRequestURI())
                        .build()
        );
    }


    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/user/{id}")
    public ResponseEntity<ApiResponse<AccountResponseForUserDTO>> updateUser(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestBody RegisterRequestDTO body) {

        AccountResponseForUserDTO result = accountService.updateUser(id, body);

        return ResponseEntity.ok(ApiResponse.<AccountResponseForUserDTO>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Account updated successfully")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('account:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            HttpServletRequest request,
            @PathVariable UUID id) {

        accountService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Account deleted successfully")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('account:read')" )
    @GetMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<AccountResponseForAdminDTO>> getUser(
            HttpServletRequest request,
            @PathVariable UUID id) {

        AccountResponseForAdminDTO result = accountService.getUser(id);

        return ResponseEntity.ok(ApiResponse.<AccountResponseForAdminDTO>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Account fetched successfully")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('account:read')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<AccountResponseForAdminDTO>>> getAllUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AccountResponseForAdminDTO> result = accountService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.<Page<AccountResponseForAdminDTO>>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Accounts fetched successfully")
                .result(result)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build());
    }

}
