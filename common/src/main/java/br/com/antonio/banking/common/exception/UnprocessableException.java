package br.com.antonio.banking.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request is well-formed but cannot be processed due to
 * business logic constraints (e.g. insufficient balance, inactive account).
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class UnprocessableException extends BusinessException {

    public UnprocessableException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "UNPROCESSABLE");
    }
}