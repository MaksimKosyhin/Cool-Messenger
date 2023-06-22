package com.example.end.domain.dto;

import com.example.end.domain.model.EntityReference;
import com.example.end.domain.model.User;
import com.example.end.domain.validation.Folders;
import com.mongodb.DBRef;

import java.util.Map;
import java.util.Set;

public record UpdateUserRequest(
        String displayName,
        String info,
        Set<User.Remainder> remainders
) {
}
