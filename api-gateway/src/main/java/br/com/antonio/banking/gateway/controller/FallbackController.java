package br.com.antonio.banking.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback responses when Circuit Breaker is OPEN.
 * Returns a structured JSON instead of a raw 503.
 */
@Slf4j
@RestController
public class FallbackController {

    @RequestMapping("/fallback/accounts")
    public ResponseEntity<Map<String, Object>> accountsFallback() {
        return buildFallback("accounts-service");
    }

    @RequestMapping("/fallback/transfers")
    public ResponseEntity<Map<String, Object>> transfersFallback() {
        return buildFallback("transfers-service");
    }

    @RequestMapping("/fallback/boletos")
    public ResponseEntity<Map<String, Object>> boletosFallback() {
        return buildFallback("boletos-service");
    }

    @RequestMapping("/fallback/pix")
    public ResponseEntity<Map<String, Object>> pixFallback() {
        return buildFallback("pix-service");
    }

    private ResponseEntity<Map<String, Object>> buildFallback(String service) {
        log.warn("Circuit breaker OPEN for: {}", service);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 503,
                "error", "Service Unavailable",
                "errorCode", "CIRCUIT_BREAKER_OPEN",
                "message", service + " is temporarily unavailable. Please try again in a few seconds.",
                "service", service
        ));
    }
}