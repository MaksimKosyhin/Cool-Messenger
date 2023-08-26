package com.example.end.domain.dto;

import com.example.end.domain.model.User;
import com.example.end.domain.validation.NotEmail;
import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Set;

public record UpdateUserRequest(
        @NotBlank(message = "display name must not be blank")
        @NotEmail
        String username,
        @NotBlank(message = "display name must not be blank")
        String displayName,
        String info,
        Map<String, Set<ObjectId>> folders,
        Set<User.Remainder> remainders
) {
}
