package com.example.end.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    protected ResponseEntity<?> handleHttpException(ApiException ex) {
        return ResponseEntity.status(ex.status).body(ex.getMessage());
    }

    @ExceptionHandler
    protected ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler
    protected ResponseEntity<?> handleAuthenticationException(AuthenticationException ex) {
        Throwable cause = ex;

        while(cause.getCause() != null) {
            cause = cause.getCause();
        }

        if(cause instanceof ApiException) {
            ApiException apiException = (ApiException) cause;
            return ResponseEntity
                    .status(apiException.status)
                    .body(apiException.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }
}
