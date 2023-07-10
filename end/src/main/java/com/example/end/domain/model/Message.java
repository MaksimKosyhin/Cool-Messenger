package com.example.end.domain.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "message-history")
@Data
public class Message {
    @Id
    private ObjectId id;

    private ObjectId chatId;

    private String text;

    private String fileUrl;

    private LocalDateTime sentAt;

    private boolean isRead;

    private boolean isEdited;

    private String senderId;
}
