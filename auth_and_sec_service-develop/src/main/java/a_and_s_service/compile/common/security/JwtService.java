package a_and_s_service.compile.common.security;

import a_and_s_service.compile.module.entity.AccountEntity;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtService {
    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        byte[] secretBytes = Decoders.BASE64.decode(props.secret());
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(AccountEntity account) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.accessTtlMs(), ChronoUnit.MILLIS);

        Set<String> authorities = new HashSet<>();
        if (account.getAccountRoles() != null) {
            account.getAccountRoles().forEach(accountRole -> {
                if (accountRole.getRole() != null) {
                    authorities.add("ROLE_" + accountRole.getRole().getCode());
                    if (accountRole.getRole().getRolePermissions() != null) {
                        accountRole.getRole().getRolePermissions().forEach(rp -> {
                            if (rp.getPermission() != null) {
                                authorities.add(rp.getPermission().getCode());
                            }
                        });
                    }
                }
            });
        }
        return Jwts.builder()
                .subject(account.getEmail())
                .claim("uid", account.getId())
                .claim("typ", "access")
                .claim("scope", String.join(" ", authorities))
                .claim("email", account.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(AccountEntity account, String jti) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.refreshTtlMs(), ChronoUnit.MILLIS);

        return Jwts.builder()
                .id(jti)
                .subject(account.getEmail())
                .claim("uid", account.getId())
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
