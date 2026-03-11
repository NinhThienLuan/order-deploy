package a_and_s_service.compile.module.service.account;

import a_and_s_service.compile.module.dto.account.request.RegisterRequestDTO;
import a_and_s_service.compile.module.dto.account.request.AdminCreateAccountRequestDTO;
import a_and_s_service.compile.module.dto.account.response.AccountResponseForAdminDTO;
import a_and_s_service.compile.module.dto.account.response.AccountResponseForUserDTO;
import a_and_s_service.compile.module.dto.forget_password.request.VerifyOTPRequestDTO;
import a_and_s_service.compile.module.entity.AccountEntity;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AccountService {

//    For Admin

    AccountResponseForAdminDTO createUserForAdmin(AdminCreateAccountRequestDTO accountCreateDTO);

    AccountResponseForAdminDTO updateUserForAdmin(UUID accountId, AdminCreateAccountRequestDTO accountCreateDTO);


//    For customer
//    Register
    void initiateRegistration (RegisterRequestDTO registerRequestDTO);
    void resendOtp(String email);
    AccountResponseForUserDTO verifyOtpAndRegister(VerifyOTPRequestDTO verifyOTPRequestDTO);

    AccountResponseForUserDTO updateUser(UUID accountId, RegisterRequestDTO registerRequestDTO);

    void deleteUser(UUID accountId);

    AccountResponseForAdminDTO getUser(UUID accountId);

    AccountEntity getUserByEmail(String email);

    Page<AccountResponseForAdminDTO> getAllUsers(int page, int size);
}
