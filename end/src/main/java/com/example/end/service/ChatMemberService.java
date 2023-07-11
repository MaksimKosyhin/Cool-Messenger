package com.example.end.service;

import com.example.end.domain.model.ChatMember;
import org.bson.types.ObjectId;

import static com.example.end.domain.model.ChatMember.*;

public interface ChatMemberService {
    public boolean hasPermission(ChatMemberId id, Permission permission);
    public ChatMember addChatCreator(ChatMemberId id);
    public ChatMember changePermissions(ChatMemberId id, ChatMember member);
    public void leaveChat(ChatMemberId id);
    public void deleteAllMembers(ObjectId chatId);
}
