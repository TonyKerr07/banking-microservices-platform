package br.com.antonio.banking.accounts.dto.response;

import br.com.antonio.banking.accounts.domain.enums.AccountStatus;
import br.com.antonio.banking.accounts.domain.enums.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String holderName,
        String documentNumber,
        String accountNumber,
        AccountType accountType,
        AccountStatus status,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt
) {}