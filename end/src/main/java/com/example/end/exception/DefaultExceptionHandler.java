package com.example.end.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DefaultExceptionHandler {
    @ExceptionHandler
    protected ResponseEntity<?> handleHttpException(ApiException ex) {
        return ResponseEntity.status(ex.status).body(ex.errors);
    }
}
