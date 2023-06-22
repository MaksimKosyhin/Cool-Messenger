package com.example.end.domain.dto;

import com.example.end.domain.model.ChatType;

public record CreateChatRequest(
        String title,
        String identifier,
        String info,
        ChatType type,
        boolean exclusive,
        boolean closed
) {
}
