package com.example.end.domain.dto;

import com.example.end.domain.model.User;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Set;

public record UpdateUserRequest(
        String username,
        String displayName,
        String info,
        Map<String, Set<ObjectId>> folders,
        Set<User.Remainder> remainders
) {
}
