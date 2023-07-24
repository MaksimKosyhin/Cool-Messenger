package com.example.end.service;

import com.example.end.domain.model.ChatMember;
import org.bson.types.ObjectId;

import java.util.Set;

import static com.example.end.domain.model.ChatMember.ChatMemberId;
import static com.example.end.domain.model.ChatMember.Permission;

public interface ChatMemberService {
    public boolean hasPermission(ChatMemberId id, Permission permission);
    public ChatMember addChatCreator(ChatMemberId id);
    public ChatMember addMember(ChatMember member);
    public Set<Permission> getPermissions(ChatMemberId id);
    public ChatMember changePermissions(ObjectId senderId, ChatMember member);
    public void leaveChat(ChatMemberId id);
    public void deleteAllMembers(ObjectId chatId);
}
