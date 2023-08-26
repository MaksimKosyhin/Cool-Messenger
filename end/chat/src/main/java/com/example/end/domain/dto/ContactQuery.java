package com.example.end.domain.dto;

public record ContactQuery(
        ContactCollection collectionName,

        AllowedField field,
        String value
) {
}
