package com.example.end.service;

import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.domain.mapper.UserEditMapper;
import com.example.end.exception.ApiException;
import com.example.end.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService{
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder decoder;
    private final UserEditMapper userEditMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(CreateUserRequest request) {
        throwIfUserExists(request);
        var user = userEditMapper.create(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        sendEmailConfirmation(user.getUsername(), user.getEmail());
    }

    private void throwIfUserExists(CreateUserRequest request) {
        if(userRepository.existsByUsername(request.username())) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    Map.of("auth", "user with this username already exists"));
        } else if(userRepository.existsByEmail(request.email())) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    Map.of("auth", "user with this email already exists"));
        }
    }

    private void sendEmailConfirmation(String username, String emailTo) {
        var token = getConfirmationToken(username);

        var subject = "Registration Confirmation";
        var confirmationUrl = "http://localhost:8080/api/v1/registration?token=" + token;
        var message = "Click on the link below to confirm accout registration";

        var email = new SimpleMailMessage();
        email.setTo(emailTo);
        email.setSubject(subject);
        email.setText(message + "\r\n" + confirmationUrl);

        mailSender.send(email);
    }

    private String getConfirmationToken(String username) {
        var now = Instant.now();
        var expiry = 60 * 60 * 24;

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
    public void confirmRegistration(String token) {
        Jwt jwt;

        try{
            jwt = decoder.decode(token);
        } catch (JwtException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, Map.of("auth", ex.getMessage()));
        }

        if(jwt.getExpiresAt().isBefore(Instant.now())) {
            var msg = "Confirmation token has expired. Try to send another confirmation";
            throw new ApiException(HttpStatus.UNAUTHORIZED, Map.of("auth", msg));
        }

        var user = userRepository.findByUsername(jwt.getSubject()).get();
        user.setEnabled(true);
        userRepository.save(user);
    }
}
