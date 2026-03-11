package fsoft.franchise.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
// import fsoft.franchise.entity.external.AccountEntity;
// import fsoft.franchise.repository.AccountRepository;
import fsoft.franchise.entity.external.AccountEntity;
import fsoft.franchise.repository.AccountRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
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

