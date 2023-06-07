package com.example.end.domain.dto;

public record UserView(
        String id,
        String displayName,
        String username,
        String imageUrl,
        String info
) {
}
