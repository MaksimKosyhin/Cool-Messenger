package com.example.end.service;

import com.example.end.domain.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface UserService {
    public AuthResponse login(AuthRequest request);
    public void register(CreateUserRequest request);
    public void confirmRegistration(String token);
    public boolean userExists(String username);
    public Path updateProfileImage(String username, MultipartFile file);
    public LoggedInUser updateUserInfo(String username, UpdateUserRequest request);
    public void changePassword(String username, UpdatePasswordRequest request);
    public void changeEmail(String username, String email);
    public void confirmEmailChange(String token);
}
