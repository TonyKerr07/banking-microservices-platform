package br.com.antonio.banking.accounts.dto.request;

import br.com.antonio.banking.accounts.domain.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(

        @NotNull(message = "Status is required")
        AccountStatus status
) {}