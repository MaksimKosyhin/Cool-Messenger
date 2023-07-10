package com.example.end.domain.dto;

import org.bson.types.ObjectId;

public record Contact(
        ObjectId id,
        String displayName,
        String identifier,
        String imageUrl,
        String info
) {
}
