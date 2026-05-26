package br.com.antonio.banking.gateway.config;

import br.com.antonio.banking.gateway.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Spring Security configuration for the reactive API Gateway.
 *
 * Public routes (no JWT required):
 * - POST /api/v1/auth/login  → token generation
 * - GET  /actuator/**        → health checks
 * - GET  /swagger-ui/**      → API documentation
 * - GET  /v3/api-docs/**     → OpenAPI specs
 *
 * All other routes require a valid Bearer JWT.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationFilter jwtFilter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Auth endpoint — public
                        .pathMatchers("/api/v1/auth/**").permitAll()
                        // Actuator — public (health checks, CI)
                        .pathMatchers("/actuator/**").permitAll()
                        // Swagger UI e OpenAPI specs — public
                        .pathMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/webjars/**"
                        ).permitAll()
                        // Downstream service docs — public
                        .pathMatchers(
                                "/*/swagger-ui/**", "/*/v3/api-docs/**"
                        ).permitAll()
                        // Everything else — requires valid JWT
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}