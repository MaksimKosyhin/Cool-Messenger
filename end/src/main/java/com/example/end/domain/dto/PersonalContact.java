package com.example.end.domain.dto;

import com.example.end.domain.model.ChatMember;
import org.bson.types.ObjectId;

import java.util.Set;

public record PersonalContact(
        ObjectId id,
        String displayName,
        String identifier,
        String imageUrl,
        String info,
        Set<ChatMember.Permission> permissions
) {
}
