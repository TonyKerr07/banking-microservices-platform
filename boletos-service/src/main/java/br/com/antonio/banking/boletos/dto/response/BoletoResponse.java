package br.com.antonio.banking.boletos.dto.response;

import br.com.antonio.banking.boletos.domain.enums.BoletoStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BoletoResponse(
        UUID id,
        String barCode,
        UUID payerAccountId,
        String beneficiaryName,
        BigDecimal amount,
        LocalDate dueDate,
        BoletoStatus status,
        String description,
        Instant createdAt,
        Instant paidAt
) {}