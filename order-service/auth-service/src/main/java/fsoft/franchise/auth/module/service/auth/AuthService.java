package fsoft.franchise.auth.module.service.auth;

import fsoft.franchise.auth.module.dto.account.request.ChangePasswordRequestDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForAdminDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForUserDTO;
import fsoft.franchise.auth.module.dto.login.request.LoginRequestDTO;
import fsoft.franchise.auth.module.dto.login.response.LoginResponseDTO;

import java.util.UUID;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    LoginResponseDTO refresh(String refreshToken);
    void logout(String refreshToken);
    void logoutAll(String email);
    void changePassword(ChangePasswordRequestDTO request);

    AccountResponseForAdminDTO getCurrentUser(String accessToken);
}
