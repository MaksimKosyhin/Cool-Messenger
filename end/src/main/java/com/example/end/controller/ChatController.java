package com.example.end.controller;

import com.example.end.domain.dto.CreateChatRequest;
import com.example.end.domain.dto.UpdateChatRequest;
import com.example.end.domain.model.ChatMember;
import com.example.end.service.ChatMemberService;
import com.example.end.service.ChatService;
import com.example.end.service.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.example.end.domain.model.ChatMember.ChatMemberId;
import static com.example.end.domain.model.ChatMember.Permission;

@Controller
@RequestMapping("api/v1/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;
    private final ChatMemberService chatMemberService;
    @PostMapping
    @Transactional
    public ResponseEntity<?> createChat(@RequestBody CreateChatRequest request) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var contact = chatService.createChat(request);
        userService.addContacts(userId, Set.of(contact.id()));
        var permissions = chatMemberService
                .addChatCreator(new ChatMemberId(contact.id(), new ObjectId(userId)))
                .getPermissions();
        contact.permissions().addAll(permissions);
        return ResponseEntity.status(HttpStatus.CREATED).body(contact);
    }

    @PutMapping("{chatId}")
    public ResponseEntity<?> updateChatInfo(@PathVariable ObjectId chatId, @RequestBody UpdateChatRequest request) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var id = new ChatMemberId(chatId, new ObjectId(userId));

        if(chatMemberService.hasPermission(id, Permission.UPDATE_CHAT_INFO)) {
            var contact = chatService.updateChatInfo(chatId, request);
            return  ResponseEntity.status(HttpStatus.OK).body(contact);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("not allowed to update chat information");
        }
    }

    @DeleteMapping("{chatId}")
    @Transactional
    public ResponseEntity<?> deleteChat(@PathVariable ObjectId chatId) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var id = new ChatMemberId(chatId, new ObjectId(userId));

        if(chatMemberService.hasPermission(id, Permission.DELETE_CHAT)) {
            chatService.deleteChat(chatId);
            chatMemberService.deleteAllMembers(chatId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("not allowed to delete chat");
        }
    }

    @PutMapping("{chatId}")
    public ResponseEntity<?> joinChat(@PathVariable ObjectId chatId) {
        var userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var permissions = chatService.getDefaultPermissions(chatId);

        var member = new ChatMember();
        member.setId(new ChatMemberId(chatId, new ObjectId(userId)));
        member.setPermissions(permissions);

        var result = chatMemberService.addMember(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("{chatId}/{receiverId}/permission")
    public ResponseEntity<?> changeUserPermissions(
            @PathVariable ObjectId chatId,
            @PathVariable ObjectId receiverId,
            @RequestBody Set<Permission> permissions) {

        var userId = SecurityContextHolder.getContext().getAuthentication().getName();

        var member = new ChatMember();
        member.setId(new ChatMemberId(chatId, receiverId));
        member.setPermissions(permissions);

        var updated= chatMemberService.changePermissions(new ObjectId(userId), member);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }
}
