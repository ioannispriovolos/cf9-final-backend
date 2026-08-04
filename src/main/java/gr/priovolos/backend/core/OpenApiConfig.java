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

/**
 * Spring configuration class responsible for configuring the
 * OpenAPI (Swagger) documentation for the REST API.
 *
 * <p>This configuration customizes the generated API documentation
 * by providing application metadata, contact information, licensing
 * details, and the JWT Bearer authentication scheme used by the
 * application.</p>
 *
 * <p>All secured endpoints automatically inherit the configured
 * security requirement, allowing authenticated API requests to be
 * executed directly from the Swagger UI.</p>
 *
 * <p>After obtaining a JWT from the authentication endpoint,
 * users can authorize Swagger requests by clicking the
 * <strong>Authorize</strong> button and supplying the token.</p>
 *
 * @author Ioannis Priovolos
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the OpenAPI configuration used by Swagger UI.
     *
     * <p>The generated documentation includes:</p>
     * <ul>
     *     <li>API title, version and description.</li>
     *     <li>Contact information.</li>
     *     <li>License information.</li>
     *     <li>Global JWT Bearer authentication configuration.</li>
     * </ul>
     *
     * <p>The configured security scheme enables the
     * <strong>Authorize</strong> button in Swagger UI, allowing
     * authenticated requests to secured REST endpoints.</p>
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Network Automation API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing secure multi-vendor network infrastructure.
                                
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
                        /*
                         * Applies JWT authentication globally so that all
                         * secured endpoints require a Bearer token.
                         */
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                        /*
                         * Defines the JWT Bearer authentication scheme used
                         * by Swagger UI.
                         *
                         * This configuration enables the "Authorize" button,
                         * allowing users to authenticate once and execute
                         * secured requests directly from the documentation.
                         */
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