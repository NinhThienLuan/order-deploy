package a_and_s_service.compile.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTtlMs,
        long refreshTtlMs
) {
}
