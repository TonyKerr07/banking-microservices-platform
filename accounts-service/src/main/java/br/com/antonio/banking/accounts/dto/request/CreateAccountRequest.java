package br.com.antonio.banking.accounts.dto.request;

import br.com.antonio.banking.accounts.domain.enums.AccountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Java record as DTO: immutable, compact, idiomatic Java 21.
 * Note: we maintain our own DTOs alongside the OpenAPI-generated models
 * because we use them internally (e.g. in service layer tests).
 * The controller receives the OpenAPI-generated model and maps to this.
 */
public record CreateAccountRequest(

        @NotBlank(message = "Holder name is required")
        @Size(min = 3, max = 150, message = "Holder name must be between 3 and 150 characters")
        String holderName,

        @NotBlank(message = "Document number is required")
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "Document must be a valid CPF (11 digits) or CNPJ (14 digits)")
        String documentNumber,

        @NotNull(message = "Account type is required")
        AccountType accountType,

        @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
        BigDecimal initialBalance
) {
    public CreateAccountRequest {
        if (initialBalance == null) initialBalance = BigDecimal.ZERO;
    }
}