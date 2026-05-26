package br.com.antonio.banking.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a downstream service call fails.
 * Maps to HTTP 503 Service Unavailable.
 */
public class ServiceUnavailableException extends BusinessException {

    public ServiceUnavailableException(String serviceName) {
        super(
                "Downstream service unavailable: " + serviceName + ". Please try again later.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "SERVICE_UNAVAILABLE"
        );
    }
}