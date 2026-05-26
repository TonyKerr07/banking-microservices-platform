package br.com.antonio.banking.pix.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountInfo(
        UUID id,
        String status,
        BigDecimal balance
) {
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}