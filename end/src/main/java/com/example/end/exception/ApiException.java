package com.example.end.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException{
    public final HttpStatus status;
    public final Map<String, String> errors;

    public ApiException(HttpStatus status, Map<String, String> errors) {
        this.status = status;
        this.errors = errors;
    }
}
