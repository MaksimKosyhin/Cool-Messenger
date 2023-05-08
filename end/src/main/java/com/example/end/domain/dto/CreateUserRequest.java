package com.example.end.domain.dto;

import com.example.end.domain.validation.UniqueEmail;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String name,
        @UniqueEmail String email,
        @NotBlank @Size(min = 5) String password) {
}
