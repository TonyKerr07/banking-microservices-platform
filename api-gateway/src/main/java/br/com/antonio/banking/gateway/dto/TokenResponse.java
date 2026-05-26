package br.com.antonio.banking.gateway.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}