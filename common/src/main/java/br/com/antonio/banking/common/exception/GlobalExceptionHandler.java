package br.com.antonio.banking.common.exception;

import br.com.antonio.banking.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Centralized exception handling for all services via Spring Auto-Configuration.
 * Each service that imports 'common' gets this handler automatically.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex,
                                                        HttpServletRequest req) {
        log.warn("Business exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ErrorResponse body = ErrorResponse.of(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getErrorCode(),
                ex.getMessage(),
                req.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        log.warn("Validation failed on {}: {} field(s) with errors", req.getRequestURI(),
                fieldErrors.size());

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.ofValidation(400, req.getRequestURI(), fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error on {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                req.getRequestURI()
        );
        return ResponseEntity.internalServerError().body(body);
    }
}