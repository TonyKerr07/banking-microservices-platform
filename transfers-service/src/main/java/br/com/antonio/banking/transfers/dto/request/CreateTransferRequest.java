package br.com.antonio.banking.transfers.dto.request;

import br.com.antonio.banking.transfers.domain.enums.TransferType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransferRequest(

        @NotNull(message = "Source account ID is required")
        UUID sourceAccountId,

        @NotNull(message = "Target account ID is required")
        UUID targetAccountId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        @Size(max = 200, message = "Description must be at most 200 characters")
        String description,

        TransferType transferType
) {
    public CreateTransferRequest {
        if (transferType == null) transferType = TransferType.INTERNAL;
    }
}