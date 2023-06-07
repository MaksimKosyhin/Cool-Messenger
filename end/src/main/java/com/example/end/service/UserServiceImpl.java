package com.example.end.service;

import com.example.end.domain.dto.*;
import com.example.end.domain.mapper.UserEditMapper;
import com.example.end.domain.mapper.UserViewMapper;
import com.example.end.domain.model.User;
import com.example.end.exception.ApiException;
import com.example.end.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final FileService fileService;
    private final JavaMailSender mailSender;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserViewMapper userViewMapper;
    private final UserEditMapper userEditMapper;

    @Override
    public Path updateProfileImage(String username, MultipartFile file) {
        var user = userRepository.findByUsername(username).get();
        var imagePath = Path.of(username, "image");

        Path fullPath;
        if(user.getImageUrl() == null) {
            fullPath = fileService.saveProfileImage(file, imagePath);
        } else {
            fullPath = fileService.replaceProfileImage(file, imagePath);
        }

        user.setImageUrl(fullPath.toString());
        userRepository.save(user);

        return fullPath;
    }

    @Override
    public LoggedInUser updateUserInfo(String username, UpdateUserRequest request) {

        if(!request.username().equals(username) && userRepository.existsByUsername(request.username())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    String.format("user with username %s already exists", request.username()));
        }

        var user = userRepository.findByUsername(username).get();
        userEditMapper.update(request, user);
        userRepository.save(user);
        return userViewMapper.toLoggedInUser(user);
    }

    @Override
    public void changePassword(String username, UpdatePasswordRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.oldPassword())
        );

        var user = (User) authentication.getPrincipal();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public UserView getUser(String userId) {
        var user = userRepository.findById(userId).orElseThrow(() ->
                new ApiException(
                        HttpStatus.NOT_FOUND,
                        String.format("user with id %s doesn't exist", userId)));

        return userViewMapper.toUserView(user);
    }


    @Override
    public void register(CreateUserRequest request) {
        throwIfUserExists(request);

        var user = userEditMapper.create(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);

        var token = getConfirmationToken(user.getId(), user.getEmail());
        sendEmailConfirmation(user.getEmail(), token);
    }

    private void throwIfUserExists(CreateUserRequest request) {
        if(userRepository.existsByUsername(request.username())) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "user with this username already exists");
        } else if(userRepository.existsByEmail(request.email())) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "user with this email already exists");
        }
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var user = (User) authentication.getPrincipal();

        var loggedInUser = userViewMapper.toLoggedInUser(user);
        var token = getAuthToken(user.getUsername());
        return new AuthResponse(loggedInUser, token);
    }

    private String getAuthToken(String username) {
        var now = Instant.now();
        var expiry = 60 * 60 * 24 * 7;

        var claims =
                JwtClaimsSet.builder()
                        .issuer("example.com")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(expiry))
                        .subject(username)
                        .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public void changeEmail(String username, String email) {
        var userId = userRepository.findIdByUsername(username);
        var token = getConfirmationToken(userId, email);
        sendEmailConfirmation(email, token);
    }

    private String getConfirmationToken(String userId, String email) {
        var now = Instant.now();
        var expiry = 60 * 60 * 24;

        var claims =
                JwtClaimsSet.builder()
                        .issuer("example.com")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(expiry))
                        .subject(userId)
                        .claim("email", email)
                        .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private void sendEmailConfirmation(String email, String token) {
        var subject = "Registration Confirmation";
        var confirmationUrl = "http://localhost:8080/api/v1/registration?token=" + token;
        var message = "Click on the link below to confirm your email";

        var mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject(subject);
        mailMessage.setText(message + "\r\n" + confirmationUrl);

        mailSender.send(mailMessage);
    }

    @Override
    public void confirmRegistration(String token) {
        var jwt = decodeFromToken(token);
        var user = userRepository.findById(jwt.getSubject()).get();
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void confirmEmailChange(String token) {
        var jwt = decodeFromToken(token);
        var user = userRepository.findById(jwt.getSubject()).get();
        user.setEmail(jwt.getClaimAsString("email"));
        userRepository.save(user);
    }

    private Jwt decodeFromToken(String token) {
        Jwt jwt;

        try{
            jwt = jwtDecoder.decode(token);
        } catch (JwtException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }

        if(jwt.getExpiresAt().isBefore(Instant.now())) {
            var msg = "Confirmation token has expired. Try to send another confirmation";
            throw new ApiException(HttpStatus.UNAUTHORIZED, msg);
        }

        return jwt;
    }
}
