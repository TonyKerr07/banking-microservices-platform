package br.com.antonio.banking.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation conflicts with the current state of the resource.
 * Example: creating an account with a document number already registered.
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }
}