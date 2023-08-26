package com.example.end.domain.dto;

public enum AllowedField {
    DISPLAY_NAME("displayName"),
    IDENTIFIER("identifier");

    AllowedField(String fieldName) {
        this.fieldName = fieldName;
    }

    public final String fieldName;
}
