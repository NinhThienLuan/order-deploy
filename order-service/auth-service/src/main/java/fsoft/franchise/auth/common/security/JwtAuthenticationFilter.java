package fsoft.franchise.auth.common.security;

import fsoft.franchise.auth.common.exception.ApiException;
import fsoft.franchise.auth.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Autowired(required = false)
    private RefreshTokenRedis refreshTokenRedis;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/api/v1/auth/refresh") || path.equals("/api/v1/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("access_token")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Bonus for fun
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseClaims(token);

            String typ = claims.get("typ", String.class);
            if (!"access".equals(typ)) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = claims.getSubject();

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Check logout time only if Redis is available
                if (refreshTokenRedis != null) {
                    Long logoutTime = refreshTokenRedis.getLogoutTime(email);
                    long issuedAt = claims.getIssuedAt().getTime();
                    if (logoutTime != null && issuedAt < logoutTime) {
                        throw new ApiException(ErrorCode.UNAUTHENTICATED,
                                "Token has been revoked due to logout or password change.");
                    }
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Failed to authenticate JWT token: {}", e.getMessage());

            // Nếu lỗi xảy ra (hết hạn, sai chữ ký...), ta xóa luôn cookie ở client
            Cookie cookieAt = new Cookie("access_token", null);
            cookieAt.setPath("/");
            cookieAt.setHttpOnly(true);
            cookieAt.setMaxAge(0); // <--- Đặt thời gian sống bằng 0 để trình duyệt xóa ngay lập tức
            response.addCookie(cookieAt);

            Cookie cookieRt = new Cookie("refresh_token", null);
            cookieRt.setPath("/api/v1/auth");
            cookieRt.setHttpOnly(true);
            cookieRt.setMaxAge(0); // <--- Đặt thời gian sống bằng 0 để trình duyệt xóa ngay lập tức
            response.addCookie(cookieRt);
            // Giảm thiểu rủi ro khi một token cũ (đã hết hạn) vẫn cứ "lởn vởn" trong máy
            // người dùng.

            // Đừng chặn request ở đây. Để filterChain tiếp tục chạy.
            // Nếu path này nằm trong whitelist, nó sẽ được cho phép.
            // Nếu không, SecurityFilterChain sẽ trả về 403 sau đó nếu không có authentication.
            filterChain.doFilter(request, response);
            return;
        }
        // Dòng này chỉ chạy khi Token HỢP LỆ
        filterChain.doFilter(request, response);
    }
}
