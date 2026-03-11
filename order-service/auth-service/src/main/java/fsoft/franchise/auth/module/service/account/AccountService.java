package fsoft.franchise.auth.module.service.account;

import fsoft.franchise.auth.module.dto.account.request.RegisterRequestDTO;
import fsoft.franchise.auth.module.dto.account.request.AdminCreateAccountRequestDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForAdminDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForUserDTO;
import fsoft.franchise.auth.module.dto.forget_password.request.VerifyOTPRequestDTO;
import fsoft.franchise.auth.module.entity.AccountEntity;
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
