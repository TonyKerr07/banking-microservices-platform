package br.com.antonio.banking.transfers.dto.response;

import br.com.antonio.banking.transfers.domain.enums.TransferStatus;
import br.com.antonio.banking.transfers.domain.enums.TransferType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String description,
        TransferType transferType,
        TransferStatus status,
        String failureReason,
        Instant createdAt
) {}