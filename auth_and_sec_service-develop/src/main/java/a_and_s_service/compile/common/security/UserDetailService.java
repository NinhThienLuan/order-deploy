package a_and_s_service.compile.common.security;

import a_and_s_service.compile.module.entity.AccountEntity;
import a_and_s_service.compile.module.enumType.StatusEnum;
import a_and_s_service.compile.module.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AccountEntity account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (account.getAccountRoles() != null) {
            account.getAccountRoles().forEach(accountRole -> {
                if (accountRole.getRole() != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + accountRole.getRole().getCode()));
                    if (accountRole.getRole().getRolePermissions() != null) {
                        accountRole.getRole().getRolePermissions().forEach(rp -> {
                            if (rp.getPermission() != null) {
                                authorities.add(new SimpleGrantedAuthority(rp.getPermission().getCode()));
                            }
                        });
                    }
                }
            });
        }
        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities(authorities)
                .build();
    }
}
