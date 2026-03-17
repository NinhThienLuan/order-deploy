package fsoft.franchise.auth.module.service.account.impl;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;
import fsoft.franchise.auth.infrastructure.cached.redis.service.forget_password.ForgetPassRedisService;
import fsoft.franchise.auth.module.entity.AccountEntity;
import fsoft.franchise.auth.module.repository.AccountRepository;
import fsoft.franchise.auth.module.service.account.AccountService;
import fsoft.franchise.auth.module.service.account.ForgetPasswordFlowService;
import fsoft.franchise.auth.module.service.email.MailSenderService;
import fsoft.franchise.auth.module.utils.GeneratedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ForgetPasswordFlowServiceImpl implements ForgetPasswordFlowService {

    private final MailSenderService mailSenderService;
    private final ForgetPassRedisService forgetPassRedisService;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public ForgetPasswordFlowServiceImpl(
            @Autowired(required = false) MailSenderService mailSenderService,
            @Autowired(required = false) ForgetPassRedisService forgetPassRedisService,
            AccountService accountService,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.mailSenderService = mailSenderService;
        this.forgetPassRedisService = forgetPassRedisService;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void processForgotPassword(String email) {
        if (forgetPassRedisService == null) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE,
                    "Password reset feature requires Redis to be configured");
        }

        if (email == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Email cannot be null");
        }
        AccountEntity account = accountService.getUserByEmail(email);

        String otp = GeneratedService.generateOTP();

        forgetPassRedisService.saveForgetPassOTP(email, otp);

        if (mailSenderService != null) {
            mailSenderService.sendOtpToEmail(email, otp);
        } else {
            log.warn("Mail service not configured. OTP for email {}: {}", email, otp);
        }
    }

    @Override
    public void resendOtp(String email) {
        if (forgetPassRedisService == null) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE,
                    "Password reset feature requires Redis to be configured");
        }
        // Check email có tồn tại trong database không, nếu tồn tại thì tạo otp mới, lưu
        // vào redis và gửi otp mới về email
        AccountEntity account = accountService.getUserByEmail(email);
        String otp = GeneratedService.generateOTP();
        forgetPassRedisService.saveForgetPassOTP(email, otp);
        if (mailSenderService != null) {
            mailSenderService.sendOtpToEmail(email, otp);
        } else {
            log.warn("Mail service not configured. OTP for email {}: {}", email, otp);
        }
    }

    @Override
    public String generateVerifyToken(String email, String otp) {
        if (forgetPassRedisService == null) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE,
                    "Password reset feature requires Redis to be configured");
        }
        // Đầu tiên để lấy ra verifyToken, ta cần phải check otp
        // Nếu otp đúng -> xóa trong redis -> tạo verifyToken mới -> lưu vào redis ->
        // trả về verifyToken cho client
        String storedOtp = forgetPassRedisService.getForgetPassOTP(email);
        if (storedOtp == null) {
            throw new ApiException(ErrorCode.OTP_EXPIRED, "OTP has expired or does not exist");
        }
        if (!storedOtp.equals(otp)) {
            throw new ApiException(ErrorCode.OTP_INVALID, "Invalid OTP");
        }
        // Khi otp hợp lệ, xóa otp cũ trong redis và tạo verifyToken mới
        forgetPassRedisService.deleteForgetPassOTP(email);
        // Tạo verifyToken và lưu vào redis và trả về cho client
        String verifyToken = GeneratedService.generateVerifyToken();
        forgetPassRedisService.saveForgetPassToken(email, verifyToken);

        return verifyToken;
    }

    @Override
    public void resetPassword(String email, String verifyToken, String newPassword) {
        if (forgetPassRedisService == null) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE,
                    "Password reset feature requires Redis to be configured");
        }
        // Check verifyToken có hợp lệ không, nếu hợp lệ thì xóa verifyToken trong redis
        // và cho phép reset password
        String storedVerifyToken = forgetPassRedisService.getForgetPassToken(email);
        if (storedVerifyToken == null) {
            throw new ApiException(ErrorCode.VERIFY_TOKEN_EXPIRED, "Verify token has expired or does not exist");
        }
        if (!storedVerifyToken.equals(verifyToken)) {
            throw new ApiException(ErrorCode.VERIFY_TOKEN_INVALID, "Invalid verify token");
        }
        forgetPassRedisService.deleteForgetPassToken(email);
        // Cho phép reset password (cập nhật password mới vào database)
        AccountEntity account = accountService.getUserByEmail(email);
        if (account == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found");
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
