package fsoft.franchise.dto.auth;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank @Email String email,
        @NotBlank String fullName,
        @NotBlank String gender,
        @NotBlank @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$") String phone

) {
}
