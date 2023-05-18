package com.example.end.controller;

import com.example.end.domain.dto.AuthRequest;
import com.example.end.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        var response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
