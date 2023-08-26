package com.example.end.domain.dto;

public enum ContactCollection {
    USERS("users"),
    CHATS("chats");

    ContactCollection(String collectionName) {
        this.collectionName = collectionName;
    }

    public final String collectionName;
}
