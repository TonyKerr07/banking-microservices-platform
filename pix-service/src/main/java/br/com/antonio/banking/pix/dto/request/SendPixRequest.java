package br.com.antonio.banking.pix.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record SendPixRequest(

        @NotNull(message = "Source account ID is required")
        UUID sourceAccountId,

        @NotBlank(message = "Target PIX key is required")
        String targetPixKey,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "PIX amount must be at least R$ 0.01")
        BigDecimal amount,

        @Size(max = 140, message = "Description must be at most 140 characters")
        String description
) {}