package br.com.antonio.banking.gateway.filter;

import br.com.antonio.banking.gateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive JWT authentication filter for Spring Cloud Gateway (WebFlux stack).
 *
 * Flow:
 * 1. Extract Bearer token from Authorization header
 * 2. Validate with JwtService
 * 3. Set authentication in ReactiveSecurityContext
 * 4. Propagate username to downstream services via X-Auth-User header
 *
 * NOTE: Uses WebFilter (reactive) — NOT OncePerRequestFilter (servlet/MVC).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            log.warn("Rejected invalid JWT from {}", exchange.getRequest().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String username = jwtService.extractUsername(token);
        log.debug("Authenticated user '{}' for path '{}'",
                username, exchange.getRequest().getPath());

        // Propagate user identity to downstream microservices
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header("X-Auth-User", username))
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, List.of());

        return chain.filter(mutatedExchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}