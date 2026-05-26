package br.com.antonio.banking.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all domain business rule violations.
 * Extend this to create specific exceptions per service.
 */
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}