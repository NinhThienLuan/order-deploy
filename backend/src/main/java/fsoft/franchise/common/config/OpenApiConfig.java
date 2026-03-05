package fsoft.franchise.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Franchise web app",
                version = "1.0",
                description = "API Documentation for Franchise Coffee App"
        ),
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(description = "Local Environment", url = "http://localhost:8080")
        },
        security = {
                @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
        },
        tags = {
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Auth",                  description = "Authentication — login, logout, and current user profile"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Products",             description = "Public product catalogue — no authentication required"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Orders",               description = "Order creation, status, history, payment processing, and refund requests"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Payments",             description = "Payment methods, history, status, creation, and gateway webhooks"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "MoMo Callbacks",       description = "Public MoMo IPN and return URL endpoints — called by MoMo server, no JWT required"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Refunds",              description = "Refund request management — FRANCHISE_ADMIN and STORE_MANAGER only"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Admin \u2014 Product",         description = "Admin product management API"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Admin \u2014 Product Image",   description = "Admin product image management API"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Admin \u2014 Product Variant", description = "Admin variant management API"),
                @io.swagger.v3.oas.annotations.tags.Tag(name = "Admin \u2014 Transactions",    description = "Internal reconciliation and reporting APIs")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    private static final List<String> TAG_ORDER = List.of(
            "Auth",
            "Products",
            "Orders",
            "Payments",
            "MoMo Callbacks",
            "Refunds",
            "Admin \u2014 Product",
            "Admin \u2014 Product Image",
            "Admin \u2014 Product Variant",
            "Admin \u2014 Transactions"
    );

    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            List<Tag> tags = openApi.getTags();
            if (tags != null) {
                tags.sort(Comparator.comparingInt(
                        tag -> {
                            int idx = TAG_ORDER.indexOf(tag.getName());
                            return idx == -1 ? Integer.MAX_VALUE : idx;
                        }
                ));
            }
        };
    }
}
