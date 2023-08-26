package com.example.end.domain.dto;

import com.example.end.domain.model.ChatMember;

import java.util.Set;

public record UpdateChatRequest(
        String title,
        String identifier,
        String info,
        Set<ChatMember.Permission> defaultPermissions
) {
}
