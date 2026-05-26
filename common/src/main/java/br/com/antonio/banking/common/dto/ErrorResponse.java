package br.com.antonio.banking.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Standard error response body for all services.
 * fieldErrors is only present on validation failures (HTTP 400).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}

    /** Factory: domain/business exceptions */
    public static ErrorResponse of(int status, String error, String errorCode,
                                   String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, errorCode, message, path, null);
    }

    /** Factory: bean validation errors */
    public static ErrorResponse ofValidation(int status, String path,
                                             List<FieldError> fieldErrors) {
        return new ErrorResponse(
                Instant.now(), status, "Validation Failed", "VALIDATION_ERROR",
                "One or more fields are invalid.", path, fieldErrors
        );
    }
}