package com.example.end.controller;

import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/v1/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody @Valid CreateUserRequest request) {
        registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<?> confirmRegistration(@RequestParam String token) {
        registrationService.confirmRegistration(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
