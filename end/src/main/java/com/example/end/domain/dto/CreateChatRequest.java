package com.example.end.domain.dto;

import com.example.end.domain.model.Chat;

public record CreateChatRequest(
        String title,
        String identifier,
        String info,
        Chat.ChatType type,
        boolean exclusive,
        boolean closed
) {
}
