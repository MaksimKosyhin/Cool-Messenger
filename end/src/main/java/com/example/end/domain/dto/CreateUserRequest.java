package com.example.end.domain.dto;

import com.example.end.domain.validation.NotEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        String displayName,
        @NotBlank(message = "password must not be blank")
        @NotEmail
        String username,
        @Email(message = "must be valid email")
        String email,
        @NotBlank(message = "must not be empty")
        @Size(min = 5, message = "must be at least 5 characters long")
        String password) {
}
