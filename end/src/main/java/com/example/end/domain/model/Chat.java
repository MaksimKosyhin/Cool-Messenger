package com.example.end.domain.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chats")
@Data
public class Chat {
    @Id
    private ObjectId id;

    private String title;

    private String identifier;

    private String info;

    private String imageUrl;

    private ChatType type;

    private boolean exclusive;

    private boolean closed;

    public enum ChatType {
        GROUP, CHANNEL, DIALOGUE;
    }
}
