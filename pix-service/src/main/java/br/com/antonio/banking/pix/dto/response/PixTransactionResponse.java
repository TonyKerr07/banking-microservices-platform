package br.com.antonio.banking.pix.dto.response;

import br.com.antonio.banking.pix.domain.enums.PixTransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PixTransactionResponse(
        UUID id,
        UUID sourceAccountId,
        String targetPixKey,
        UUID targetAccountId,
        BigDecimal amount,
        String description,
        PixTransactionStatus status,
        String endToEndId,
        Instant createdAt
) {}