package com.example.end.domain.dto;

public record ContactQuery<E>(
        Class<E> source,
        String field,
        String value
) {
}
