package fsoft.franchise.common.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS Configuration
 * 
 * NOTE: CORS is now configured in api-gateway
 * to avoid duplicate CORS headers. This class is kept for reference but is
 * empty.
 * 
 * Previous issue: Having CORS configured in both WebSecurityConfig and
 * CorsConfig
 * caused duplicate 'Access-Control-Allow-Origin' headers.
 */
@Configuration
public class CorsConfig {
    // CORS configuration moved to api-gateway to prevent duplicate headers
}
