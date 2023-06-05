package com.example.end.service;

import com.example.end.domain.dto.CreateUserRequest;

public interface RegistrationService {
    public void register(CreateUserRequest request);
    public void confirmRegistration(String token);
}
