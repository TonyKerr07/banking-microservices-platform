package br.com.antonio.banking.pix.dto.request;

import br.com.antonio.banking.pix.domain.enums.PixKeyType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePixKeyRequest(

        @NotNull(message = "Account ID is required")
        UUID accountId,

        @NotNull(message = "Key type is required")
        PixKeyType keyType,

        @Size(max = 77, message = "Key value must be at most 77 characters")
        String keyValue
) {}