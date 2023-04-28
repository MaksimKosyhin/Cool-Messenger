package com.example.end.domain.model;

import com.mongodb.DBRef;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "chat-members")
@Data
public class ChatMembers {
    private DBRef fromId;
    private Set<Member> members;

    public class Member {
        private String userId;
        private Role role;
    }

    public enum Role {
        OWNER, MODERATOR, USER, WAITING, BANNED
    }
}
