package gr.priovolos.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Value("${allowed.origins}")
    private List<String> allowedOrigins;

    @Value("${security.bcrypt.strength}")
    private int bcryptStrength;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider)
            throws Exception {
        http
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                                .requestMatchers(HttpMethod.POST, "/api/v1/teachers").permitAll()           // register
                                .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/{uuid}").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/users/{uuid}").hasAuthority("EDIT_USER")
                                .requestMatchers(HttpMethod.GET, "/api/v1/users/allusers").hasAuthority("VIEW_USERS")
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/authenticate").permitAll()
                                .requestMatchers("/api/v1/automation/**").authenticated()                   // This blocks ALL unauthenticated traffic completely at the gate
                                .requestMatchers("/api/v1/eligible/**").permitAll()
                                .requestMatchers(
                                        "/",                       // Core app landing root
                                        "/favicon.ico",            // Default browser favicon requests
                                        "/error",                  // Spring Boot default error routing path
                                        "/swagger-ui.html",        // The old Swagger UI HTML (if used)
                                        "/swagger-ui/**",          // All Swagger UI resources (JS, CSS, etc.)
                                        "/v3/api-docs/**",         // The API JSON docs
                                        "/v3/api-docs.yaml",       // YAML version of the docs
                                        "/swagger-resources/**",   // Swagger resource descriptors
                                        "/configuration/**",       // Swagger configuration endpoints
                                        "/webjars/**"              // CRITICAL: Webjars files where Swagger UI CSS/JS assets are packed
                                ).permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/v1/teachers/{uuid}").hasAnyAuthority("VIEW_TEACHER", "VIEW_ONLY_TEACHER")
//                                .requestMatchers(HttpMethod.GET, "/api/v1/teachers/*").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/v1/teachers/{uuid}/*").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/v1/users/*").permitAll()
//                                .requestMatchers(HttpMethod.PUT, "/api/v1/teachers/{uuid}").hasAuthority("EDIT_TEACHER")
//                                .requestMatchers(HttpMethod.PATCH, "/api/v1/teachers/{uuid}").hasAuthority("DELETE_TEACHER")

//                                .requestMatchers(HttpMethod.GET, "/api/v1/teachers").hasAuthority("VIEW_TEACHERS")
//                                .requestMatchers(HttpMethod.GET, "/api/v1/teachers/*").hasAnyAuthority("VIEW_TEACHER", "VIEW_ONLY_TEACHER")
//                                .requestMatchers(HttpMethod.GET, "/api/v1/teachers/*").permitAll()
//                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(myCustomAuthenticationEntryPoint())
                        .accessDeniedHandler(myCustomAccessDeniedHandler()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    // application.properties CORS works only without Spring Security.

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // AccessDeniedHandler (Handles 403 Forbidden)
    // Triggered when an authenticated user tries to access a resource they don’t have permissions for.
    // Returns HTTP 403 Forbidden with a basic error page.
    @Bean
    public AccessDeniedHandler myCustomAccessDeniedHandler() {
        return new CustomAccessDeniedHandler(objectMapper);
    }

    // AuthenticationEntryPoint (Handles 401 Unauthorized)
    // Triggered when an unauthenticated user tries to access a secured resource.
    // Default behavior: Returns HTTP 401 (for APIs).
    @Bean
    public AuthenticationEntryPoint myCustomAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

}