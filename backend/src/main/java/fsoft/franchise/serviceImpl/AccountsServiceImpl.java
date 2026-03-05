package fsoft.franchise.serviceImpl;

import fsoft.franchise.common.exception.ApiException;
import fsoft.franchise.exception.CommonErrorCode;
import fsoft.franchise.security.JwtProperties;
import fsoft.franchise.security.JwtService;
import fsoft.franchise.dto.auth.LoginRequest;
import fsoft.franchise.dto.auth.AccountResponse;
import fsoft.franchise.dto.auth.LoginResponse;
import fsoft.franchise.dto.auth.PermissionResponse;
import fsoft.franchise.dto.auth.RoleResponse;
import fsoft.franchise.entity.external.AccountEntity;
import fsoft.franchise.enums.StatusEnum;
import fsoft.franchise.repository.AccountRepository;
import fsoft.franchise.service.AccountsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountsServiceImpl implements AccountsService {

    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. Delegate credential check to Spring Security
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw new ApiException(CommonErrorCode.UNAUTHORIZED, "Invalid email or password");
        } catch (AuthenticationException e) {
            throw new ApiException(CommonErrorCode.UNAUTHORIZED, "Invalid email or password");
        }

        // 2. Load full account entity
        AccountEntity account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(CommonErrorCode.UNAUTHORIZED));

        // 3. Reject inactive accounts
        if (account.getStatus() != StatusEnum.ACTIVE) {
            throw new ApiException(CommonErrorCode.FORBIDDEN, "Account is inactive or disabled");
        }

        // 4. Issue tokens
        String jti = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account, jti);
        long expiresIn = jwtProperties.accessTtlMs() / 1000; // convert to seconds for clients

        return new LoginResponse(accessToken, refreshToken, expiresIn);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getMe(String email) {
        AccountEntity account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(CommonErrorCode.UNAUTHORIZED));
        return toAccountResponse(account);
    }

    @Override
    public void logout() {
        // No server-side state (no Redis). The controller clears the cookie.
        // This hook exists for future token blacklisting if needed.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Manual mapping (no MapStruct configured for auth domain in this project)
    // ─────────────────────────────────────────────────────────────────────────
    private AccountResponse toAccountResponse(AccountEntity account) {
        List<RoleResponse> roles = null;
        if (account.getAccountRoles() != null) {
            roles = account.getAccountRoles().stream()
                    .filter(ar -> ar.getRole() != null)
                    .map(ar -> {
                        var role = ar.getRole();
                        List<PermissionResponse> permissions = null;
                        if (role.getRolePermissions() != null) {
                            permissions = role.getRolePermissions().stream()
                                    .filter(rp -> rp.getPermission() != null)
                                    .map(rp -> new PermissionResponse(
                                            rp.getPermission().getId(),
                                            rp.getPermission().getModule(),
                                            rp.getPermission().getCode()))
                                    .toList();
                        }
                        return new RoleResponse(
                                role.getId(),
                                role.getCode(),
                                role.getName(),
                                role.getDescription());
                    })
                    .toList();
        }

        var profile = account.getProfile();
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getPhoneNumber(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getLastName() : null,
                profile != null ? profile.getGender() : null,
                profile != null ? profile.getBirthDate() : null,
                roles);
    }
}
