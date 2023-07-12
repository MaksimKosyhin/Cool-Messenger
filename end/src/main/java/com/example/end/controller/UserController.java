package com.example.end.controller;

import com.example.end.domain.dto.AuthRequest;
import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.domain.dto.UpdatePasswordRequest;
import com.example.end.domain.dto.UpdateUserRequest;
import com.example.end.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        var response = userService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //todo: check chat with given identifier not exists
    @PostMapping
    public ResponseEntity<?> register(@RequestBody @Valid CreateUserRequest request) {
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
        var id = SecurityContextHolder.getContext().getAuthentication().getName();
        var updated = userService.updateUserInfo(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
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
