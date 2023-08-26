package com.example.end.domain.dto;

import com.example.end.domain.model.User;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Set;

public record LoggedInUser(
        String displayName,
        String username,
        String info,
        String imagePath,
        Set<User.Remainder> remainders,
        Set<ObjectId> contacts,
        Map<String, Set<ObjectId>> folders
) {
}
