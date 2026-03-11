package fsoft.franchise.auth.module.dto.forget_password.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OTPRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email
) {
}
