package com.example.end.domain.dto;

import org.bson.types.ObjectId;

public record ChatMembersQuery(
        ObjectId chatId,
        String field,
        String value
) {
}
