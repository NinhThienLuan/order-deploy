package a_and_s_service.compile.module.controller;

import a_and_s_service.compile.common.exception.ErrorCode;
import a_and_s_service.compile.common.response.ApiResponse;
import a_and_s_service.compile.module.dto.forget_password.request.OTPRequestDTO;
import a_and_s_service.compile.module.dto.forget_password.request.TokenAndPasswordRequestDTO;
import a_and_s_service.compile.module.dto.forget_password.request.VerifyOTPRequestDTO;
import a_and_s_service.compile.module.service.account.ForgetPasswordFlowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/forget-password")
@RequiredArgsConstructor
public class ForgetPasswordController {
    private final ForgetPasswordFlowService forgetPasswordFlowService;

    // Bước 1: Nhập email -> gửi OTP về email
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody OTPRequestDTO request) {
        forgetPasswordFlowService.processForgotPassword(request.email());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(1000)
                        .message("OTP đã được gửi đến email của bạn")
                        .build()
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(HttpServletRequest request, @Valid @RequestParam String email) {
        forgetPasswordFlowService.resendOtp(email);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(ErrorCode.SUCCESS.getCode())
                        .message("OTP đã được gửi lại đến email của bạn")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    // Bước 2: Nhập OTP -> xác thực và lấy verifyToken
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOTPRequestDTO request) {
        String verifyToken = forgetPasswordFlowService.generateVerifyToken(request.email(), request.otp());
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(1000)
                        .message("OTP hợp lệ")
                        .result(verifyToken)
                        .build()
        );
    }

    // Bước 3: Dùng verifyToken + mật khẩu mới -> reset password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody TokenAndPasswordRequestDTO request) {
        forgetPasswordFlowService.resetPassword(request.email(), request.verifyToken(), request.newPassword());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(1000)
                        .message("Đổi mật khẩu thành công")
                        .build()
        );
    }
}
