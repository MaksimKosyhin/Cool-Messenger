package com.example.end.domain.dto;

import com.example.end.domain.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record LoggedInUser(
        String username,
        String qualifier,
        String info,
        String imageUrl,
        Set<User.Remainder> remainders,
        Map<String, List<String>> folders
) {
}
