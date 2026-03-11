package a_and_s_service.compile.module.dto.forget_password.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyOTPRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "OTP is required")
        String otp) {
}
