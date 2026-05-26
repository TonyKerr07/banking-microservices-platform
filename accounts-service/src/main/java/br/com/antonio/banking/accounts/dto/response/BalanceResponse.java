package br.com.antonio.banking.accounts.dto.response;

import br.com.antonio.banking.accounts.domain.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceResponse(
        UUID accountId,
        String accountNumber,
        BigDecimal balance,
        AccountStatus status,
        Instant consultedAt
) {}