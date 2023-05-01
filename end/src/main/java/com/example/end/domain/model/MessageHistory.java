package com.example.end.domain.model;

import com.mongodb.DBRef;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@Document(collection = "message-history")
@Data
public class MessageHistory {

    private DBRef fromId;

    private List<Message> messages;

    @Data
    public static class Message {
        private String text;

        private String fileUrl;

        private LocalDateTime sentAt;

        private boolean isRead;

        private boolean isEdited;

        private String senderId;
    }
}
