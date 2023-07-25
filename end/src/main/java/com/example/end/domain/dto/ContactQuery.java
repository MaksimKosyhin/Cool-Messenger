package com.example.end.domain.dto;

public record ContactQuery(
        String collectionName,
        String field,
        String value
) {
}
