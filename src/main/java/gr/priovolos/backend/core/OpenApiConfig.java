package gr.priovolos.backend.core;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/*
  The practical effect is that Swagger UI shows an "Authorize" button where you paste your JWT token.
 */
public class OpenApiConfig {

    /*
        Provides the metadata that appears in Swagger UI's header section —
        purely informational, no functional impact on the API itself
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Network Automation API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing secure multi-vendor hardware orchestration infrastructure.
                                
                                Authentication is done via JWT Bearer tokens.
                                Obtain a token from /api/auth/authenticate before using secured endpoints.
                        """)
                        .contact(new Contact()
                                .name("Coding Factory @ AUEB")
                                .email("priovolosg@aueb.gr")
                                .url("https://codingfactory.aueb.gr"))
                        .license(new License()
                                .name("CC0 1.0 Universal")
                                .url("https://creativecommons.org/publicdomain/zero/1.0")))
                        // 1. Apply the authorization requirement globally to all endpoints
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                        // 2. Define how the Authorize button expects to receive credentials
                        .components(new Components()
                                .addSecuritySchemes("bearerAuth",
                                        new SecurityScheme()
                                                .name("bearerAuth")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Paste your generated JWT access token here to authorize endpoints matching your user roles.")));

    }
}