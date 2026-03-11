package fsoft.franchise.auth.module.dto.forget_password.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TokenAndPasswordRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "OTP is required")
        String verifyToken,

        @NotBlank(message = "New password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character")
        String newPassword
) {
}
