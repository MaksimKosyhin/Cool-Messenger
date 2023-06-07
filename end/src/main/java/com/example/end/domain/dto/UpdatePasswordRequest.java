package com.example.end.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
        String oldPassword,
        @Size(min = 5)
        String newPassword
) {
}
