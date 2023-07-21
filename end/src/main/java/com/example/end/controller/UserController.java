package com.example.end.controller;

import com.example.end.domain.dto.AuthRequest;
import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.domain.dto.UpdatePasswordRequest;
import com.example.end.domain.dto.UpdateUserRequest;
import com.example.end.service.ChatService;
import com.example.end.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Controller
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ChatService chatService;

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        var response = userService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody @Valid CreateUserRequest request) {
        if(userService.existsByUsername(request.username()) ||
            chatService.existsByIdentifier(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("username: %s is occupied", request.username()));
        }

        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("confirm")
    public ResponseEntity<?> confirmRegistration(@RequestParam String token) {
        userService.confirmRegistration(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping
    public ResponseEntity<?> updateUserInfo(@RequestBody @Valid UpdateUserRequest request) {
        if(userService.existsByUsername(request.username()) ||
                chatService.existsByIdentifier(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("username: %s is occupied", request.username()));
        }

        var id = SecurityContextHolder.getContext().getAuthentication().getName();
        var updated = userService.updateUserInfo(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @PutMapping("contacts")
    public ResponseEntity<?> removeContacts(@RequestBody Set<ObjectId> contacts) {
        var id = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userService.removeContacts(id, contacts);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("image")
    public ResponseEntity<?> updateProfileImage(@RequestParam("file") MultipartFile file) {
        var id = SecurityContextHolder.getContext().getAuthentication().getName();
        var imagePath = userService.updateProfileImage(id, file);
        return ResponseEntity.status(HttpStatus.OK).body(imagePath);
    }

    @PutMapping("password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid UpdatePasswordRequest request) {
        var id = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changePassword(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("email")
    public ResponseEntity<?> changeEmail(@RequestBody String email) {
        var id = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changeEmail(id, email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("email/confirm")
    public ResponseEntity<?> confirmEmailChange(@RequestParam String token) {
        userService.confirmEmailChange(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
