package br.com.antonio.banking.transfers.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight DTO to deserialize the accounts-service response.
 * We only map what transfers-service needs — avoids tight coupling.
 */
public record AccountInfo(
        UUID id,
        String status,
        BigDecimal balance
) {
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}