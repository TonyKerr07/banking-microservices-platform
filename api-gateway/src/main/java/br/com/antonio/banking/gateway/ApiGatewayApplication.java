package br.com.antonio.banking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — Spring Cloud Gateway (WebFlux/reactive).
 *
 * Responsibilities:
 * - Route requests to downstream microservices
 * - CORS handling (single entry point for frontend)
 * - Request/response logging
 * - Slot for future: JWT auth filter, rate limiting, circuit breaker
 *
 * IMPORTANT: This module uses WebFlux (reactive stack).
 * Do NOT import spring-boot-starter-web or any Spring MVC dependency.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}