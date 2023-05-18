package com.example.end.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        String displayName,
        @NotBlank String username,
        @Email String email,
        @NotBlank @Size(min = 5) String password) {
}
