package com.example.end.domain.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "chat-members")
@Data
public class ChatMember {
    @Id
    private ChatMemberId id;
    private Set<Permission> permissions;

    public enum Permission {
        DELETE_CHAT,
        MAKE_MODERATOR,
        REMOVE_MODERATOR,
        BAN,
        DELETE_ALL_MESSAGES,
        SEND_MESSAGE,
        DELETE_PERSONAL_MESSAGES,
        EDIT_PERSONAL_MESSAGES,
        WAITING,
        BANNED
    }

    public static record ChatMemberId(ObjectId chatId, ObjectId userId){}
}
