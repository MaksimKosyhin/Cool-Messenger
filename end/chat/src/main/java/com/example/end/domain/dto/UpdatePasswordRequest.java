package com.example.end.domain.dto;

import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        String oldPassword,
        @Size(min = 5, message = "should be at least 5 characters long")
        String newPassword
) {
}
