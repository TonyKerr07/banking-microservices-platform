package br.com.antonio.banking.boletos.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBoletoRequest(

        @NotNull(message = "Payer account ID is required")
        UUID payerAccountId,

        @NotBlank(message = "Beneficiary name is required")
        @Size(max = 150)
        String beneficiaryName,

        String beneficiaryDocument,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least R$ 0.01")
        BigDecimal amount,

        @NotNull(message = "Due date is required")
        @Future(message = "Due date must be in the future")
        LocalDate dueDate,

        @Size(max = 200)
        String description
) {}