package fsoft.franchise.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
// import fsoft.franchise.entity.external.AccountEntity;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
// import java.sql.Date;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    // nap secret Key vao trong JwtUtil
    public JwtService(JwtProperties props) {
        this.props = props;
        byte[] secretBytes = decodeSecret(props.secret());
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    private byte[] decodeSecret(String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("app.jwt.secret must not be blank");
        }

        try {
            return Decoders.BASE64.decode(secret);
        } catch (DecodingException ignored) {
        }

        try {
            return Decoders.BASE64URL.decode(secret);
        } catch (DecodingException ignored) {
        }

        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public String getTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "access_token");
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    /**
     * Resolve JWT from request: cookie first, then Authorization Bearer header.
     * Aligns with JwtAuthenticationFilter and BE_Refactor (Cookie + Header
     * fallback).
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String token = getTokenFromCookie(request);
        if (StringUtils.hasText(token))
            return token;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7).trim();
        return null;
    }

    /** @return the raw uid string claim (account ID as UUID string) */
    public String getUid(String token) {
        if (token == null || token.isEmpty())
            return null;
        return parseClaims(token).get("uid", String.class);
    }

    /**
     * @return the account ID as a {@link UUID}, or {@code null} if the token is
     *         blank
     *         or the uid claim is missing/malformed.
     */
    public UUID getUserId(String token) {
        String uid = getUid(token);
        if (uid == null || uid.isBlank())
            return null;
        try {
            return UUID.fromString(uid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @return the email (subject) embedded in the token, or {@code null} if blank.
     */
    public String getEmail(String token) {
        if (token == null || token.isEmpty())
            return null;
        return parseClaims(token).getSubject();
    }

    /**
     * @return the raw scope string (space-separated authorities), kept for
     *         backward compatibility.
     */
    public String getRole(String token) {
        if (token == null || token.isEmpty())
            return null;
        return parseClaims(token).get("scope", String.class);
    }

    /**
     * Primary role for business logic: first "ROLE_xxx" in scope, with "ROLE_"
     * stripped.
     * E.g. scope "ROLE_ADMIN READ_ORDERS" → "ADMIN".
     * Use this when comparing to ADMIN / MANAGER / CUSTOMER.
     */
    public String getPrimaryRole(String token) {
        String scope = getRole(token);
        if (scope == null || scope.isBlank())
            return null;
        String[] parts = scope.split(" ");
        // 1. Try to find one with ROLE_ prefix
        for (String s : parts) {
            String trimmed = s.trim();
            if (trimmed.startsWith("ROLE_"))
                return trimmed.substring(5);
        }
        // 2. Fallback to common roles if no ROLE_ prefix exists
        for (String s : parts) {
            String trimmed = s.trim();
            if (Arrays.asList("ADMIN", "MANAGER", "CUSTOMER", "POS").contains(trimmed)) {
                return trimmed;
            }
        }
        // 3. Last resort: first element
        return parts[0].trim().startsWith("ROLE_") ? parts[0].trim().substring(5) : parts[0].trim();
    }

    /**
     * @return all granted authorities (ROLE_xxx + permission codes) as a
     *         {@link Set}, parsed from the space-separated {@code scope} claim.
     *         Never {@code null}.
     */
    public Set<String> getRoles(String token) {
        String scope = getRole(token);
        if (scope == null || scope.isBlank())
            return Collections.emptySet();
        return new HashSet<>(Arrays.asList(scope.split(" ")));
    }

    /** @return {@code true} if the token's expiration is in the past. */
    public boolean isTokenExpired(String token) {
        if (token == null || token.isEmpty())
            return true;
        java.util.Date expiration = parseClaims(token).getExpiration();
        return expiration.before(new java.util.Date());
    }

    public long getRemainingExpirationInMs(String token) {
        Claims claims = parseClaims(token);
        java.util.Date expiration = claims.getExpiration();
        return Math.max(0, expiration.getTime() - System.currentTimeMillis());
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
