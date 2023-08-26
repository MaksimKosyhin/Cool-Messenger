package com.example.end.service;

import com.example.end.domain.model.ChatMember;
import com.example.end.exception.ApiException;
import com.example.end.repository.ChatMemberRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.example.end.domain.model.ChatMember.ChatMemberId;
import static com.example.end.domain.model.ChatMember.Permission;

@Service
@RequiredArgsConstructor
public class ChatMemberServiceImpl implements ChatMemberService{

    private final  ChatMemberRepository chatMemberRepository;

    @Override
    public boolean hasPermission(ChatMemberId id, Permission permission) {
        var member = getMemberOrThrow(id);
        return member.getPermissions().contains(permission);
    }

    @Override
    public ChatMember addChatCreator(ChatMemberId id) {
        var chatMember = new ChatMember();
        chatMember.setId(id);
        chatMember.setPermissions(Permission.getAll());
        return chatMemberRepository.save(chatMember);
    }

    @Override
    public ChatMember addMember(ChatMember member) {
        return chatMemberRepository.save(member);
    }

    @Override
    public Set<Permission> getPermissions(ChatMemberId id) {
        return getMemberOrThrow(id).getPermissions();
    }

    @Override
    public ChatMember changePermissions(ObjectId senderId, ChatMember member) {
        var sender = getMemberOrThrow(new ChatMemberId(member.getId().chatId(), senderId));

        if(sender.getPermissions().contains(Permission.CHANGE_PERMISSIONS) &&
                sender.getPermissions().containsAll(member.getPermissions())) {
            return chatMemberRepository.save(member);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    String.format(
                            "user with id: %s is not allowed change permissions of user with id: %s",
                            sender.getId().userId(),
                            member.getId().userId()));
        }
    }

    @Override
    public void leaveChat(ChatMemberId id) {
        chatMemberRepository.deleteById(id);
    }

    @Override
    public void deleteAllMembers(ObjectId chatId) {
        chatMemberRepository.deleteChatMembers(chatId);
    }

    private ChatMember getMemberOrThrow(ChatMemberId id) {
        return chatMemberRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        String.format("chat with id: %s can't be found")));
    }
}
