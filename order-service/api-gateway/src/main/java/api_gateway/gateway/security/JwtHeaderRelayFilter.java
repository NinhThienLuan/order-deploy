package api_gateway.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtHeaderRelayFilter implements GlobalFilter, Ordered {

    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_SCOPE = "X-User-Scope";
    private static final String HEADER_USER_ID = "X-User-Id";

    private final SecretKey signingKey;

    public JwtHeaderRelayFilter(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = buildSigningKey(secret);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate().headers(headers -> {
            headers.remove(HEADER_USER_EMAIL);
            headers.remove(HEADER_USER_SCOPE);
            headers.remove(HEADER_USER_ID);
        });

        String token = resolveToken(exchange.getRequest().getHeaders(), exchange.getRequest().getCookies().getFirst("access_token"));
        if (StringUtils.hasText(token)) {
            injectHeadersIfValid(token, requestBuilder);
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private void injectHeadersIfValid(String token, ServerHttpRequest.Builder requestBuilder) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();

            if (!"access".equals(claims.get("typ", String.class))) {
                return;
            }

            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                return;
            }

            String uid = claims.get("uid", String.class);
            String email = claims.get("email", String.class);
            if (!StringUtils.hasText(email)) {
                email = claims.getSubject();
            }
            String scope = claims.get("scope", String.class);

            if (!StringUtils.hasText(uid) || !StringUtils.hasText(email) || !StringUtils.hasText(scope)) {
                return;
            }

            final String finalUid = uid;
            final String finalEmail = email;
            final String finalScope = scope;

            requestBuilder.headers(headers -> {
                headers.set(HEADER_USER_ID, finalUid);
                headers.set(HEADER_USER_EMAIL, finalEmail);
                headers.set(HEADER_USER_SCOPE, finalScope);
            });
        } catch (Exception ignored) {
            // Keep request unauthenticated; downstream service can return 401.
        }
    }

    private String resolveToken(HttpHeaders headers, HttpCookie accessTokenCookie) {
        if (accessTokenCookie != null && StringUtils.hasText(accessTokenCookie.getValue())) {
            return accessTokenCookie.getValue().trim();
        }

        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        return null;
    }

    private SecretKey buildSigningKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

