package com.example.end.service;

import com.example.end.exception.ApiException;
import com.example.end.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public void sendEmailConfirmation(String emailTo) {
        var token = getConfirmationToken(emailTo);

        var subject = "Registration Confirmation";
        var confirmationUrl = "http://localhost:8080/api/v1/registration?token=" + token;
        var message = "Click on the link below to confirm accout registration";

        var email = new SimpleMailMessage();
        email.setTo(emailTo);
        email.setSubject(subject);
        email.setText(message + "\r\n" + confirmationUrl);

        mailSender.send(email);
    }

    private String getConfirmationToken(String email) {
        var now = Instant.now();
        var expiry = 60 * 60 * 24;

        var claims =
                JwtClaimsSet.builder()
                        .issuer("example.com")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(expiry))
                        .subject(email)
                        .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

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

        var user = userRepository.findByEmail(jwt.getSubject()).get();
        user.setEnabled(true);
        userRepository.save(user);
    }

}
