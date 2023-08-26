package com.example.end.domain.dto;

import com.example.end.domain.model.Chat;
import com.example.end.domain.model.ChatMember;

import java.util.Set;

public record CreateChatRequest(
        String title,
        String identifier,
        String info,
        Chat.ChatType type,
        boolean exclusive,
        boolean closed,
        Set<ChatMember.Permission> defaultPermissions
) {
}
