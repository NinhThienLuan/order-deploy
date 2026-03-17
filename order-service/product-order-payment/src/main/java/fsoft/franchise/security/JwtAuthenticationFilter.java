package fsoft.franchise.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for product-order-payment microservice.
 * Extracts JWT token from request and populates Spring Security context with
 * authorities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Extract JWT token from request (cookie or Authorization header)
        String token = jwtService.getTokenFromRequest(request);

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Parse JWT claims
            Claims claims = jwtService.parseClaims(token);

            // Verify it's an access token
            String typ = claims.get("typ", String.class);
            if (!"access".equals(typ)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check if token is expired
            if (jwtService.isTokenExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = claims.getSubject();
            String uid = claims.get("uid", String.class);
            String scope = claims.get("scope", String.class);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Parse authorities from scope (auth-service: ROLE_ADMIN, ROLE_MANAGER, ROLE_USER, ROLE_POS)
                List<SimpleGrantedAuthority> authorities = (scope == null || scope.isBlank())
                        ? new java.util.ArrayList<>()
                        : Arrays.stream(scope.split(" "))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                // Create authentication token with authorities

                // Create authentication token with authorities
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Set authentication for user: {} with authorities: {}", email, authorities);
            }
        } catch (Exception e) {
            log.error("JWT authentication failed for token: {} - Error: {}", 
                token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null", 
                e.getMessage());
            // Continue filter chain without setting authentication
        }

        filterChain.doFilter(request, response);
    }

}
