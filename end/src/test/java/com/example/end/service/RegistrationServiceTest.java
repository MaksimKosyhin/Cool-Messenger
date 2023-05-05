package com.example.end.service;

import com.example.end.domain.model.User;
import com.example.end.exception.ApiException;
import com.example.end.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private JwtEncoder encoder;

    @Mock
    private JwtDecoder decoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    public void confirmRegistration_withValidToken_shouldEnableUser() {
        String token = "sample_token";
        String email = "test@example.com";
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(86400);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("example.com")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(email)
                .build();
        Jwt jwt = new Jwt(token, now, expiry, Map.of("alg", "HS256"), claims.getClaims());

        when(decoder.decode(token)).thenReturn(jwt);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        User expectedUser = new User();
        expectedUser.setEnabled(true);

        registrationService.confirmRegistration(token);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(expectedUser);
    }

    @Test
    public void confirmRegistration_withExpiredToken_shouldThrowApiException() {
        String token = "sample_token";
        String email = "test@example.com";
        Instant twoDaysAgo = Instant.now().minusSeconds(60 * 60 * 24 * 2);
        Instant yesterday = twoDaysAgo.plusSeconds(60 * 60 * 24);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("example.com")
                .issuedAt(twoDaysAgo)
                .expiresAt(yesterday)
                .subject(email)
                .build();
        Jwt jwt = new Jwt(token, twoDaysAgo, yesterday, Map.of("alg", "HS256"), claims.getClaims());

        when(decoder.decode(token)).thenReturn(jwt);

        var msg = "Confirmation token has expired. Try to send another confirmation";

        assertThatThrownBy(() -> registrationService.confirmRegistration(token))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED)
                .hasFieldOrPropertyWithValue("errors", Map.of("auth", msg));
    }

    @Test
    public void confirmRegistration_withInvalidToken_shouldThrowApiException() {
        String token = "sample_token";

        when(decoder.decode(token)).thenThrow(new JwtException("Invalid token"));

        assertThatThrownBy(() -> registrationService.confirmRegistration(token))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.UNAUTHORIZED)
                .hasFieldOrPropertyWithValue("errors", Map.of("auth", "Invalid token"));
    }
}

