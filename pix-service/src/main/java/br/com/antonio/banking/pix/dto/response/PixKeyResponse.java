package br.com.antonio.banking.pix.dto.response;

import br.com.antonio.banking.pix.domain.enums.PixKeyStatus;
import br.com.antonio.banking.pix.domain.enums.PixKeyType;

import java.time.Instant;
import java.util.UUID;

public record PixKeyResponse(
        UUID id,
        UUID accountId,
        PixKeyType keyType,
        String keyValue,
        PixKeyStatus status,
        Instant createdAt
) {}