package com.example.end.controller;

import com.example.end.domain.dto.*;
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
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var updated = userService.updateUserInfo(username, request);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    @PutMapping("image")
    public ResponseEntity<?> updateProfileImage(@RequestParam("file") MultipartFile file) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var imagePath = userService.updateProfileImage(username, file);
        return ResponseEntity.status(HttpStatus.OK).body(imagePath);
    }

    @PutMapping("password")
    public ResponseEntity<?> changePassword(@RequestBody UpdatePasswordRequest request) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changePassword(username, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("email")
    public ResponseEntity<?> changeEmail(@RequestBody String email) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changeEmail(username, email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("email/confirm")
    public ResponseEntity<?> confirmEmailChange(@RequestParam String token) {
        userService.confirmEmailChange(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        var user = userService.getUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
