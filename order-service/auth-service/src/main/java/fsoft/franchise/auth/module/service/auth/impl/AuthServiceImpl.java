package fsoft.franchise.auth.module.service.auth.impl;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;
import fsoft.franchise.auth.common.security.JwtProperties;
import fsoft.franchise.auth.common.security.JwtService;
import fsoft.franchise.auth.common.security.RefreshTokenRedis;
import fsoft.franchise.auth.module.dto.account.request.ChangePasswordRequestDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForAdminDTO;
import fsoft.franchise.auth.module.dto.account.response.AccountResponseForUserDTO;
import fsoft.franchise.auth.module.dto.login.request.LoginRequestDTO;
import fsoft.franchise.auth.module.dto.login.response.LoginResponseDTO;
import fsoft.franchise.auth.module.entity.AccountEntity;
import fsoft.franchise.auth.module.enumType.StatusEnum;
import fsoft.franchise.auth.module.mapper.AccountMapper;
import fsoft.franchise.auth.module.repository.AccountRepository;
import fsoft.franchise.auth.module.service.auth.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRedis refreshTokenRedis;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (UsernameNotFoundException e) {
            throw new ApiException(ErrorCode.INVALID_INFO, "Email Invalid");
        } catch (BadCredentialsException e) {
            throw new ApiException(ErrorCode.INVALID_INFO, "Password Invalid");
        } catch (AuthenticationException e) {
            throw new ApiException(ErrorCode.INVALID_INFO);
        }
        AccountEntity account = accountRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INFO));

        if(account.getStatus() != StatusEnum.ACTIVE) {
            throw new ApiException(ErrorCode.INVALID_INFO, "Account is InACTIVE");
        }
        return generateAuthResponse(account);
    }


    @Override
    public LoginResponseDTO refresh(String refreshToken) {
        try {
            // 1. Giải mã và kiểm tra loại token
            Claims claims = jwtService.parseClaims(refreshToken);
            if (!"refresh".equals(claims.get("typ"))) {
                throw new ApiException(ErrorCode.UNAUTHENTICATED);
            }

            // 2. Kiểm tra token có tồn tại trong Redis không (chống token đã logout)
            String jti = claims.getId();
            String userIdStr = refreshTokenRedis.getUserIdByJti(jti)
                    .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED));

            // 3. Lấy User mới nhất từ DB để đảm bảo họ vẫn ACTIVE
            AccountEntity account = accountRepository.findById(UUID.fromString(userIdStr))
                    .filter(a -> a.getStatus() == StatusEnum.ACTIVE)
                    .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INFO));

            // 4. (Tùy chọn) Xóa token cũ để thực hiện Token Rotation (Bảo mật hơn)
            refreshTokenRedis.revoke(jti);

            // 5. Cấp bộ token mới
            return generateAuthResponse(account);

        } catch (Exception e) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public void logout(String refreshToken) {
        try {
            Claims claims = jwtService.parseClaims(refreshToken);
            String jti = claims.getId();
            refreshTokenRedis.revoke(jti);
        } catch (Exception e) {

        }
    }

    @Override
    public void logoutAll(String email) {
        AccountEntity account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INFO));
       this.executeLogoutAll(account);
    }

    private void executeLogoutAll(AccountEntity account) {
        refreshTokenRedis.revokeAllTokens(account.getId().toString());
        refreshTokenRedis.setLogoutTime(account.getEmail());
    }

    // Gom nhóm logic tạo Token và lưu Redis
    private LoginResponseDTO generateAuthResponse(AccountEntity account) {
        String jti = UUID.randomUUID().toString();

        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account, jti);

        // Lưu Refresh Token vào Redis với thời gian sống (TTL) tương ứng
        refreshTokenRedis.store(
                jti,
                account.getId().toString(),
                Duration.ofMillis(jwtProperties.refreshTtlMs())
        );

        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                jwtProperties.accessTtlMs() / 1000 // Chuyển sang giây cho Frontend
        );
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDTO request){
        if(!request.newPassword().equals(request.confirmPassword())) {
            throw new ApiException(ErrorCode.INVALID_INFO, "Confirm password does not match new password");
        }

        if(request.newPassword().equals(request.oldPassword())) {
            throw new ApiException(ErrorCode.INVALID_INFO, "New password must be different from old password");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        AccountEntity account = accountRepository.findByEmail(email).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED));

        if(!passwordEncoder.matches(request.oldPassword(), account.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_INFO, "Old password is incorrect");
        }

        account.setPassword(passwordEncoder.encode(request.newPassword()));
        accountRepository.save(account);
        this.executeLogoutAll(account);
    }

    @Override
    public AccountResponseForAdminDTO getCurrentUser(String accessToken) {
        // 1. Giải mã và kiểm tra loại token
        Claims claims = jwtService.parseClaims(accessToken);
        if (!"access".equals(claims.get("typ"))) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }
        // 3. Lấy TRỰC TIẾP userId từ cục claims vừa giải mã (KHÔNG cần qua hàm nào nữa)
        String userId = claims.get("uid", String.class);
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Token không chứa thông tin User");
        }

//        // 2. Kiểm tra token có tồn tại trong Redis không (chống token đã logout)
//        String jti = claims.getId();
//        String userIdStr = refreshTokenRedis.getUserIdByJti(jti)
//                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED));

        // 3. Lấy User mới nhất từ DB để đảm bảo họ vẫn ACTIVE
        AccountEntity account = accountRepository.findById(UUID.fromString(userId))
                .filter(a -> a.getStatus() == StatusEnum.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INFO));

        return accountMapper.toResponseDTO(account);
    }

}
