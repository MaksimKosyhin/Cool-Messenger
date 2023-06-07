package com.example.end.domain.dto;

import com.example.end.domain.model.User;
import com.example.end.domain.validation.Folders;
import com.example.end.domain.validation.Remainders;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record UpdateUserRequest(

        String displayName,
        @NotBlank
        String username,
        String info,
        @Remainders
        Set<User.Remainder> remainders,
        @Folders
        Map<String, List<String>> folders
) {
}
