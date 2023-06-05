package com.example.end.service;

import com.example.end.domain.dto.AuthRequest;
import com.example.end.domain.dto.AuthResponse;
@FunctionalInterface
public interface AuthService {
    public AuthResponse login(AuthRequest request);
}
