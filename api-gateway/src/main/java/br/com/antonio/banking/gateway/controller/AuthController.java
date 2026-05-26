package br.com.antonio.banking.gateway.controller;

import br.com.antonio.banking.gateway.dto.LoginRequest;
import br.com.antonio.banking.gateway.dto.TokenResponse;
import br.com.antonio.banking.gateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Authentication endpoint — issues JWT Bearer tokens.
 *
 * IMPORTANT: This uses hardcoded users for portfolio/demo purposes.
 * In a production system, replace this with a dedicated auth-service
 * that validates credentials against a database (e.g. with BCrypt).
 *
 * Demo credentials:
 *   antonio / banking@2024
 *   admin    / admin123
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    // Demo users — replace with DB lookup in production
    private static final Map<String, String> DEMO_USERS = Map.of(
            "antonio", "banking@2024",
            "admin",   "admin123"
    );

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        String storedPassword = DEMO_USERS.get(request.username());

        if (storedPassword == null || !storedPassword.equals(request.password())) {
            log.warn("Failed login attempt for user: {}", request.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(request.username());
        log.info("Token issued for user: {}", request.username());

        return ResponseEntity.ok(new TokenResponse(
                token,
                "Bearer",
                jwtService.getExpiration()
        ));
    }
}