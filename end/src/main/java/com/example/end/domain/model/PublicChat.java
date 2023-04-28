package com.example.end.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chats")
@Data
public class PublicChat {
    @Id
    private String id;

    private String title;

    private String info;

    private String imageUrl;

    private String identifier;

    private Type type;

    private boolean exclusive;

    private boolean closed;

    public enum Type{
        GROUP, CHANNEL
    }
}
