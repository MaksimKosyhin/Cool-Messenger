package com.example.end.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException{
    public final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        } else if(!(obj instanceof ApiException)) {
            return false;
        } else {
            ApiException other = (ApiException) obj;
            if(this.status.equals(other.status) && this.getMessage().equals(other.getMessage())) {
                return true;
            }
        }

        return false;
    }
}
