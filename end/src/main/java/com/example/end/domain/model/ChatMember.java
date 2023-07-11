package com.example.end.domain.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Document(collection = "chat-members")
@Data
public class ChatMember {
    @Id
    private ChatMemberId id;
    private Set<Permission> permissions;

    public enum Permission {
        CHANGE_PERMISSIONS,
        DELETE_CHAT,
        ACCESS_CHAT,
        UPDATE_CHAT_INFO,
        DELETE_ALL_MESSAGES,
        SEND_MESSAGE,
        DELETE_PERSONAL_MESSAGES,
        EDIT_PERSONAL_MESSAGES
    }

    public static record ChatMemberId(ObjectId chatId, ObjectId userId){}
}
