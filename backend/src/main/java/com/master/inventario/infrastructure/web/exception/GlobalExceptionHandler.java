package com.master.inventario.infrastructure.web.exception;

import com.master.inventario.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidation(DomainException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "DOMAIN_VALIDATION_ERROR",
                ex.getMessage(),
                Instant.now(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "REQUEST_VALIDATION_ERROR",
                message,
                Instant.now(),
                request.getRequestURI()
        ));
    }
}

