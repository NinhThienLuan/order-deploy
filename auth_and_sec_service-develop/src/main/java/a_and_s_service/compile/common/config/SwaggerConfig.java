package a_and_s_service.compile.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "E-Commerce API Assignment",
                version = "1.0",
                description = "API Documentation for E-Commerce App"
        ),
        servers = {
                @Server(description = "Local Environment", url = "http://localhost:8080")
        },
        // Áp dụng bảo mật cho toàn bộ API
        security = {
                @SecurityRequirement(name = "bearerAuth"),
                @SecurityRequirement(name = "cookieAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT via Authorization header (Bearer token)",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@SecurityScheme(
        name = "cookieAuth",
        description = "JWT via access_token cookie",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "access_token"
)
public class SwaggerConfig {
}
