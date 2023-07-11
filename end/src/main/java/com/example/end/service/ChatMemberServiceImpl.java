package com.example.end.service;

import com.example.end.domain.model.ChatMember;
import com.example.end.exception.ApiException;
import com.example.end.repository.ChatMemberRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;

import static com.example.end.domain.model.ChatMember.*;

import java.util.Set;

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
        var permissions = Set.of(
                Permission.ACCESS_CHAT,
                Permission.SEND_MESSAGE,
                Permission.CHANGE_PERMISSIONS,
                Permission.DELETE_CHAT,
                Permission.DELETE_ALL_MESSAGES,
                Permission.DELETE_PERSONAL_MESSAGES,
                Permission.EDIT_PERSONAL_MESSAGES,
                Permission.UPDATE_CHAT_INFO
        );

        var chatMember = new ChatMember();
        chatMember.setId(id);
        chatMember.setPermissions(permissions);
        return chatMemberRepository.save(chatMember);
    }

    @Override
    public ChatMember changePermissions(ChatMemberId id, ChatMember member) {
        var suggester = getMemberOrThrow(id);

        if(id.chatId().equals(member.getId().chatId()) &&
                suggester.getPermissions().contains(Permission.CHANGE_PERMISSIONS) &&
                suggester.getPermissions().containsAll(member.getPermissions())) {
            return chatMemberRepository.save(member);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    String.format(
                            "user with id: %s is not allowed change permissions of user with id: %s",
                            id.userId(),
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
