package com.example.end.domain.dto;

public record UpdateChatRequest(
        String title,
        String identifier,
        String info
) {
}
