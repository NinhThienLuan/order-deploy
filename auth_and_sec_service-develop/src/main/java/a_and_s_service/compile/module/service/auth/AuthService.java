package a_and_s_service.compile.module.service.auth;

import a_and_s_service.compile.module.dto.account.request.ChangePasswordRequestDTO;
import a_and_s_service.compile.module.dto.account.response.AccountResponseForAdminDTO;
import a_and_s_service.compile.module.dto.account.response.AccountResponseForUserDTO;
import a_and_s_service.compile.module.dto.login.request.LoginRequestDTO;
import a_and_s_service.compile.module.dto.login.response.LoginResponseDTO;

import java.util.UUID;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    LoginResponseDTO refresh(String refreshToken);
    void logout(String refreshToken);
    void logoutAll(String email);
    void changePassword(ChangePasswordRequestDTO request);

    AccountResponseForAdminDTO getCurrentUser(String accessToken);
}
