package com.example.end.domain.model;

import com.mongodb.DBRef;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "chat-members")
@Data
public class ChatMembers {
    private String chatId;
    private Set<Member> members;

    @Data
    public static class Member {
        private String userId;
        private Set<Permission> permissions;
    }

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
}
