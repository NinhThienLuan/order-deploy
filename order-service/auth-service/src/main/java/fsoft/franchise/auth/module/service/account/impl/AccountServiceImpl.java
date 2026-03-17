package fsoft.franchise.auth.module.service.account.impl;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;
import fsoft.franchise.auth.infrastructure.cached.redis.service.register.RegisterRedisService;
import fsoft.franchise.auth.module.dto.account.request.RegisterRequestDTO;
import fsoft.franchise.auth.module.dto.account.request.AdminCreateAccountRequestDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForAdminDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForUserDTO;
import fsoft.franchise.auth.module.dto.forget_password.request.VerifyOTPRequestDTO;
import fsoft.franchise.auth.module.entity.AccountEntity;
import fsoft.franchise.auth.module.entity.AccountRoleEntity;
import fsoft.franchise.auth.module.entity.ProfileEntity;
import fsoft.franchise.auth.module.entity.RoleEntity;
import fsoft.franchise.auth.module.enumType.StatusEnum;
import fsoft.franchise.auth.module.mapper.AccountMapper;
import fsoft.franchise.auth.module.repository.AccountRepository;
import fsoft.franchise.auth.module.repository.ProfileRepository;
import fsoft.franchise.auth.module.service.account.AccountService;
import fsoft.franchise.auth.module.service.account_role.AccountRoleService;
import fsoft.franchise.auth.module.service.email.MailSenderService;
import fsoft.franchise.auth.module.service.role_permission.RoleService;
import fsoft.franchise.auth.module.utils.GeneratedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final AccountRoleService accountRoleService;
    private final MailSenderService mailSenderService;
    private final RegisterRedisService registerRedisService;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            ProfileRepository profileRepository,
            AccountMapper accountMapper,
            PasswordEncoder passwordEncoder,
            RoleService roleService,
            AccountRoleService accountRoleService,
            @Autowired(required = false) MailSenderService mailSenderService,
            @Autowired(required = false) RegisterRedisService registerRedisService) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.accountRoleService = accountRoleService;
        this.mailSenderService = mailSenderService;
        this.registerRedisService = registerRedisService;
    }

    @Override
    public AccountResponseForAdminDTO createUserForAdmin(AdminCreateAccountRequestDTO accountRequestDTO) {
        if (accountRequestDTO == null) {
            log.error("accountCreateDTO is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "AccountCreateDTO cannot be null");
        }

        AccountEntity account = accountMapper.adminCreateToAccountEntity(accountRequestDTO);
        account.setStatus(StatusEnum.ACTIVE);
        account.setPassword(passwordEncoder.encode(accountRequestDTO.password()));

        AccountEntity savedAccount = accountRepository.save(account);

        ProfileEntity profile = accountMapper.adminCreateToProfileEntity(accountRequestDTO);
        profile.setAccount(savedAccount);
        profileRepository.save(profile);

        savedAccount.setProfile(profile);

        List<RoleEntity> roles = roleService.getAllRoleByIds(accountRequestDTO.roleIds());
        if (roles.size() != accountRequestDTO.roleIds().size()) {
            log.error("One or more roles not found for IDs: {}", accountRequestDTO.roleIds());
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "One or more roles not found");
        }
        Set<AccountRoleEntity> accountRoles = roles.stream()
                .map(role -> AccountRoleEntity.builder()
                        .account(savedAccount)
                        .role(role)
                        .build())
                .collect(Collectors.toSet());

        accountRoleService.saveAllAccountRole(accountRoles);

        savedAccount.setAccountRoles(accountRoles);

        return accountMapper.toResponseDTO(savedAccount);
    }

    @Transactional
    @Override
    public AccountResponseForAdminDTO updateUserForAdmin(UUID accountId,
            AdminCreateAccountRequestDTO accountCreateDTO) {
        if (accountId == null || accountCreateDTO == null) {
            log.error("accountId or accountCreateDTO is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "AccountId and AccountCreateDTO cannot be null");
        }

        AccountEntity foundedAccount = accountRepository.findByIdWithDetails(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found"));

        // Kiểm tra duplicate email/phoneNumber — chỉ check nếu khác account hiện tại
        if (accountCreateDTO.email() != null
                && !accountCreateDTO.email().equals(foundedAccount.getEmail())
                && accountRepository.existsByEmail(accountCreateDTO.email())) {
            log.error("Email {} is already in use by another account", accountCreateDTO.email());
            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Email is already in use by another account");
        }
        if (accountCreateDTO.phoneNumber() != null
                && !accountCreateDTO.phoneNumber().equals(foundedAccount.getPhoneNumber())
                && accountRepository.existsByPhoneNumber(accountCreateDTO.phoneNumber())) {
            log.error("Phone number {} is already in use by another account", accountCreateDTO.phoneNumber());
            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Phone number is already in use by another account");
        }

        // Update account fields — chỉ update nếu field != null
        if (accountCreateDTO.email() != null) {
            foundedAccount.setEmail(accountCreateDTO.email());
        }
        if (accountCreateDTO.phoneNumber() != null) {
            foundedAccount.setPhoneNumber(accountCreateDTO.phoneNumber());
        }
        if (accountCreateDTO.password() != null) {
            foundedAccount.setPassword(passwordEncoder.encode(accountCreateDTO.password()));
        }

        // Update profile fields — tạo mới profile nếu chưa có
        ProfileEntity profile = foundedAccount.getProfile();
        if (profile == null) {
            profile = new ProfileEntity();
            profile.setAccount(foundedAccount);
            foundedAccount.setProfile(profile);
        }
        if (accountCreateDTO.firstName() != null) {
            profile.setFirstName(accountCreateDTO.firstName());
        }
        if (accountCreateDTO.lastName() != null) {
            profile.setLastName(accountCreateDTO.lastName());
        }
        if (accountCreateDTO.gender() != null) {
            profile.setGender(accountCreateDTO.gender());
        }
        if (accountCreateDTO.birthDate() != null) {
            profile.setBirthDate(accountCreateDTO.birthDate());
        }

        // Update roles nếu có truyền roleIds
        if (accountCreateDTO.roleIds() != null && !accountCreateDTO.roleIds().isEmpty()) {
            List<RoleEntity> newRoles = roleService.getAllRoleByIds(accountCreateDTO.roleIds());
            if (newRoles.size() != accountCreateDTO.roleIds().size()) {
                log.error("One or more roles not found for IDs: {}", accountCreateDTO.roleIds());
                throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "One or more roles not found");
            }

            // =========================================================
            // FIX LỖI TẠI ĐÂY: Dùng clear() và addAll() của Collection
            // =========================================================

            // 1. Dọn sạch rổ của Hibernate (Nhờ orphanRemoval, nó sẽ TỰ ĐỘNG bắn lệnh
            // DELETE xuống DB)
            if (foundedAccount.getAccountRoles() != null) {
                foundedAccount.getAccountRoles().clear();
            }

            // 2. Tạo dàn đệ mới
            Set<AccountRoleEntity> newAccountRoles = newRoles.stream()
                    .map(role -> AccountRoleEntity.builder()
                            .account(foundedAccount)
                            .role(role)
                            .build())
                    .collect(Collectors.toSet());

            // 3. Nạp dàn đệ mới vào lại rổ
            if (foundedAccount.getAccountRoles() == null) {
                foundedAccount.setAccountRoles(newAccountRoles); // Nếu rổ ban đầu null thì mới set
            } else {
                foundedAccount.getAccountRoles().addAll(newAccountRoles); // Nhét thêm vào rổ đã clear
            }

        }

        AccountEntity updatedAccount = accountRepository.save(foundedAccount);
        return accountMapper.toResponseDTO(updatedAccount);
    }

    // @Transactional
    // @Override
    // public AccountResponseForAdminDTO updateUserForAdmin(UUID accountId,
    // AdminCreateAccountRequestDTO accountCreateDTO) {
    // if (accountId == null || accountCreateDTO == null) {
    // log.error("accountId or accountCreateDTO is null");
    // throw new ApiException(ErrorCode.INVALID_INPUT, "AccountId and
    // AccountCreateDTO cannot be null");
    // }
    // AccountEntity foundedAccount =
    // accountRepository.findByIdWithDetails(accountId)
    //
    // .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account
    // not found"));
    //
    //// Kiểm tra duplicate email/phoneNumber — chỉ check nếu khác account hiện tại
    // if (accountCreateDTO.email() != null
    // && !accountCreateDTO.email().equals(foundedAccount.getEmail())
    // && accountRepository.existsByEmail(accountCreateDTO.email())) {
    // log.error("Email {} is already in use by another account",
    // accountCreateDTO.email());
    // throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Email is already in use
    // by another account");
    // }
    //
    // if (accountCreateDTO.phoneNumber() != null
    // && !accountCreateDTO.phoneNumber().equals(foundedAccount.getPhoneNumber())
    // && accountRepository.existsByPhoneNumber(accountCreateDTO.phoneNumber())) {
    // log.error("Phone number {} is already in use by another account",
    // accountCreateDTO.phoneNumber());
    // throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Phone number is already
    // in use by another account");
    // }
    //// Update account fields — chỉ update nếu field != null
    // if (accountCreateDTO.email() != null) {
    // foundedAccount.setEmail(accountCreateDTO.email());
    // }
    // if (accountCreateDTO.phoneNumber() != null) {
    // foundedAccount.setPhoneNumber(accountCreateDTO.phoneNumber());
    // }
    // if (accountCreateDTO.password() != null) {
    // foundedAccount.setPassword(passwordEncoder.encode(accountCreateDTO.password()));
    // }
    //// Update profile fields — tạo mới profile nếu chưa có
    // ProfileEntity profile = foundedAccount.getProfile();
    // if (profile == null) {
    // profile = new ProfileEntity();
    // profile.setAccount(foundedAccount);
    // foundedAccount.setProfile(profile);
    // }
    // if (accountCreateDTO.firstName() != null) {
    // profile.setFirstName(accountCreateDTO.firstName());
    // }
    // if (accountCreateDTO.lastName() != null) {
    // profile.setLastName(accountCreateDTO.lastName());
    // }
    //
    // if (accountCreateDTO.gender() != null) {
    // profile.setGender(accountCreateDTO.gender());
    // }
    // if (accountCreateDTO.birthDate() != null) {
    // profile.setBirthDate(accountCreateDTO.birthDate());
    // }
    //// Update roles nếu có truyền roleIds
    // if (accountCreateDTO.roleIds() != null &&
    // !accountCreateDTO.roleIds().isEmpty()) {
    // List<RoleEntity> newRoles =
    // roleService.getAllRoleByIds(accountCreateDTO.roleIds());
    // if (newRoles.size() != accountCreateDTO.roleIds().size()) {
    // log.error("One or more roles not found for IDs: {}",
    // accountCreateDTO.roleIds());
    // throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "One or more roles not
    // found");
    // }
    //// Xóa thẳng xuống DB trước — tránh lỗi duplicate identity trong persistence
    // context
    // accountRoleService.deleteByAccountId(accountId);

    //// Tạo và lưu roles mới
    // Set<AccountRoleEntity> newAccountRoles = newRoles.stream()
    // .map(role -> AccountRoleEntity.builder()
    // .account(foundedAccount)
    // .role(role)
    // .build()
    // )
    // .collect(Collectors.toSet());
    // accountRoleService.saveAllAccountRole(newAccountRoles);
    // foundedAccount.setAccountRoles(newAccountRoles);
    // }
    // AccountEntity updatedAccount = accountRepository.save(foundedAccount);
    // return accountMapper.toResponseDTO(updatedAccount);
    // }

    @Override
    public void initiateRegistration(RegisterRequestDTO registerRequestDTO) {
        if (registerRedisService == null) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE,
                    "Registration feature requires Redis to be configured");
        }
        String email = registerRequestDTO.email();

        if (accountRepository.existsByEmail(email)) {
            log.error("Email {} is already registered", email);
            throw new ApiException(ErrorCode.DUPLICATE_RESOURCE, "Account is already registered");
        }

        String otp = GeneratedService.generateOTP();
        // Lưu OTP và data vào Redis với TTL 5' cho otp, 30' cho data
        try {
            registerRedisService.saveRegisterData(email, registerRequestDTO);
        } catch (Exception e) {
            log.error("Error retrieving registration data for email {}: {}", email, e.getMessage());
            throw new ApiException(ErrorCode.REQUEST_FAILED, "Error retrieving registration data, please try again");
        }
        registerRedisService.saveRegisterOtp(email, otp);

        // Gửi OTP qua email
        if (mailSenderService != null) {
            mailSenderService.sendOtpToEmail(email, otp);
            log.info("OTP has been sent to email: {}", otp);
        } else {
            log.warn("Mail service not configured. OTP for email {}: {}", email, otp);
        }
    }

    @Override
    public void resendOtp(String email) {
        if (registerRedisService == null) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE,
                    "Registration feature requires Redis to be configured");
        }

        // Flow để resend otp, là check data của email này còn trong redis không
        // Nếu còn thì lấy email trong data đó để gửi lại otp mới
        // Check data trong redis
        RegisterRequestDTO data = null;
        try {
            data = registerRedisService.getRegisterData(email);
            if (data == null) {
                log.error("No registration data found for email: {}", email);
                throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "No registration data found for this email, please register again");
            }
        } catch (Exception e) {
            log.error("Error retrieving registration data for email {}: {}", email, e.getMessage());
            throw new ApiException(ErrorCode.REQUEST_FAILED, "Error retrieving registration data, please try again");
        }

        // Gửi lại otp mới dựa trên data email lấy được
        String otp = GeneratedService.generateOTP();
        registerRedisService.saveRegisterOtp(otp, email);
        if (mailSenderService != null) {
            mailSenderService.sendOtpToEmail(email, otp);
            log.info("OTP has been resent to email: {}", otp);
        } else {
            log.warn("Mail service not configured. OTP for email {}: {}", email, otp);
        }
    }

    @Override
    public AccountResponseForUserDTO verifyOtpAndRegister(VerifyOTPRequestDTO verifyOTPRequestDTO) {
        if (registerRedisService == null) {
            throw new ApiException(ErrorCode.SERVICE_UNAVAILABLE,
                    "Registration feature requires Redis to be configured");
        }
        String email = verifyOTPRequestDTO.email();
        String otp = verifyOTPRequestDTO.otp();

        // Check OTP
        String storedOtp = registerRedisService.getRegisterOtp(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            log.error("Invalid OTP {} for email {}", otp, email);
            throw new ApiException(ErrorCode.INVALID_INPUT, "Invalid OTP");
        }

        // Lấy data đăng ký từ Redis
        // edit by Luan team 1 catch exception
        RegisterRequestDTO registerData = null;
        try {
            registerData = registerRedisService.getRegisterData(email);
            if (registerData == null) {
                log.error("No registration data found for email: {}", email);
                throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "No registration data found for this email, please register again");
            }
        } catch (Exception e) {
            log.error("Error retrieving registration data for email {}: {}", email, e.getMessage());
            throw new ApiException(ErrorCode.REQUEST_FAILED, "Error retrieving registration data, please try again");
        }

        // Tạo tài khoản mới
        AccountEntity account = accountMapper.registerToAccountEntity(registerData);
        account.setStatus(StatusEnum.ACTIVE);
        account.setPassword(passwordEncoder.encode(registerData.password()));

        AccountEntity savedAccount = accountRepository.save(account);

        ProfileEntity profile = accountMapper.registerToProfileEntity(registerData);
        profile.setAccount(savedAccount);
        profileRepository.save(profile);

        // Set ngược profile vào savedAccount để mapper có thể map các trường profile
        savedAccount.setProfile(profile);

        // Phân quyền cứng cho khách là CUSTOMER
        RoleEntity customerRole = roleService.getRoleByCode("USER");
        if (customerRole == null) {
            log.error("Customer role not found in database");
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Customer role not found, please contact support");
        }
        AccountRoleEntity accountRole = new AccountRoleEntity();
        accountRole.setAccount(savedAccount);
        accountRole.setRole(customerRole);

        accountRoleService.saveCustomerAccountRole(accountRole);
        savedAccount.setAccountRoles(Set.of(accountRole));

        // Xóa data và OTP trong Redis sau khi đăng ký thành công
        registerRedisService.clearRegisterSession(email);

        return accountMapper.toUserResponseDTO(savedAccount);
    }

    @Transactional
    @Override
    public AccountResponseForUserDTO updateUser(UUID accountId, RegisterRequestDTO registerRequestDTO) {
        if (accountId == null || registerRequestDTO == null) {
            log.error("accountId or accountUpdateDTO is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "AccountId and AccountUpdateDTO cannot be null");
        }
        AccountEntity foundedAccount = accountRepository.findByIdWithDetails(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found"));

        // Update account fields — chỉ update nếu field != null
        if (registerRequestDTO.email() != null) {
            foundedAccount.setEmail(registerRequestDTO.email());
        }
        if (registerRequestDTO.phoneNumber() != null) {
            foundedAccount.setPhoneNumber(registerRequestDTO.phoneNumber());
        }
        if (registerRequestDTO.password() != null) {
            foundedAccount.setPassword(passwordEncoder.encode(registerRequestDTO.password()));
        }

        // Update profile fields
        ProfileEntity profile = foundedAccount.getProfile();
        if (profile != null) {
            if (registerRequestDTO.firstName() != null) {
                profile.setFirstName(registerRequestDTO.firstName());
            }
            if (registerRequestDTO.lastName() != null) {
                profile.setLastName(registerRequestDTO.lastName());
            }
            if (registerRequestDTO.gender() != null) {
                profile.setGender(registerRequestDTO.gender());
            }
            if (registerRequestDTO.birthDate() != null) {
                profile.setBirthDate(registerRequestDTO.birthDate());
            }
        }

        AccountEntity updatedAccount = accountRepository.save(foundedAccount);
        return accountMapper.toUserResponseDTO(updatedAccount);
    }

    @Override
    public void deleteUser(UUID accountId) {
        if (accountId == null) {
            log.error("accountId is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "AccountId cannot be null");
        }
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found"));

        account.setStatus(StatusEnum.INACTIVE);
        account.setDeletedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    public AccountResponseForAdminDTO getUser(UUID accountId) {
        if (accountId == null) {
            log.error("accountId is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "AccountId cannot be null");
        }
        AccountEntity account = accountRepository.findByIdWithDetails(accountId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Account not found"));

        return accountMapper.toResponseDTO(account);
    }

    @Override
    public AccountEntity getUserByEmail(String email) {
        if (email == null) {
            log.error("email is null");
            throw new ApiException(ErrorCode.INVALID_INPUT, "Email cannot be null");
        }
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Account not found, please register first"));
    }

    @Override
    public Page<AccountResponseForAdminDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accountRepository.findAllWithDetails(pageable)
                .map(accountMapper::toResponseDTO);
    }
}
