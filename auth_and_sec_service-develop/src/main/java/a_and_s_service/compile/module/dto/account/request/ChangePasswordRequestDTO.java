package a_and_s_service.compile.module.dto.account.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequestDTO (
        @NotBlank(message = "Old password must not be blank")
        String oldPassword,
        @NotBlank(message = "New password must not be blank")
        String newPassword,
        @NotBlank(message = "Confirm password must not be blank")
        String confirmPassword
){
}
