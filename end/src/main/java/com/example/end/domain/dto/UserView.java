package com.example.end.domain.dto;

import com.mongodb.DBRef;

public record UserView(
        DBRef ref,
        String displayName,
        String username,
        String imageUrl,
        String info
) {
}
