package fsoft.franchise.common.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
@SecurityScheme(name = "bearerAuth", description = "JWT auth description", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {

        @Value("${server.url:http://localhost:8080}")
        private String serverUrl;

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
                        "Admin \u2014 Transactions");

        @Bean
        public OpenAPI customOpenAPI() {
                Server localServer = new Server();
                localServer.setUrl("http://localhost:8081");
                localServer.setDescription("Local Environment");

                Server gatewayServer = new Server();
                gatewayServer.setUrl("http://localhost:8080");
                gatewayServer.setDescription("Gateway Environment");

                return new OpenAPI()
                                .info(new Info()
                                                .title("Franchise web app")
                                                .version("1.0")
                                                .description("API Documentation for Franchise Coffee App"))
                                .servers(List.of(gatewayServer, localServer))
                                .security(List.of(
                                                new io.swagger.v3.oas.models.security.SecurityRequirement()
                                                                .addList("bearerAuth")));
        }

        @Bean
        public OpenApiCustomizer tagOrderCustomizer() {
                return openApi -> {
                        List<Tag> tags = openApi.getTags();
                        if (tags != null) {
                                tags.sort(Comparator.comparingInt(
                                                tag -> {
                                                        int idx = TAG_ORDER.indexOf(tag.getName());
                                                        return idx == -1 ? Integer.MAX_VALUE : idx;
                                                }));
                        }
                };
        }
}
