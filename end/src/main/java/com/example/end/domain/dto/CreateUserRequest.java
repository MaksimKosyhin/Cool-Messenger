package com.example.end.domain.dto;

import com.example.end.domain.validation.UniqueEmail;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @UniqueEmail String email,
        @NotBlank @Min(5) String password) {
}
