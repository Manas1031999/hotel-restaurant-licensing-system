package in.wb.nhls.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// Swagger UI: /swagger-ui.html; উপরের Authorize বাটনে login-এর token বসালে সব request-এ যায়
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI nhlsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel & Restaurant Licensing System API")
                        .version("v1")
                        .description("""
                                Business registration, license applications with a configurable review workflow,
                                licenses, payments, audit logs and notifications.

                                1. POST /api/v1/auth/login (admin / Admin@12345 locally)
                                2. Copy data.accessToken
                                3. Click **Authorize** and paste the token
                                """))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
