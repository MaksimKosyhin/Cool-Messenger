package com.example.end.domain.dto;

import com.example.end.domain.model.EntityReference;
import com.mongodb.DBRef;

public record Contact(
        String id,
        String displayName,
        String identifier,
        String imageUrl,
        String info
) {
}
