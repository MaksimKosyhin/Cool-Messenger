package com.example.end.domain.dto;

import com.example.end.domain.model.User;
import com.mongodb.DBRef;

import java.util.Map;
import java.util.Set;

public record LoggedInUser(
        String displayName,
        String username,
        String info,
        String imageUrl,
        Set<User.Remainder> remainders,
        Map<String, Set<String>> folders,
        Set<Contact> contacts
) {
}
