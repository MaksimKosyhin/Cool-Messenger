package com.example.end.controller;

import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RegistrationControllerTest {

    private static final String REGISTRATION_PATH = "/api/v1/users";

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    private final JwtEncoder encoder;

    private final UserRepository userRepository;

    @Autowired
    public RegistrationControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, JwtEncoder encoder, UserRepository userRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.encoder = encoder;
        this.userRepository = userRepository;
    }

    @AfterEach
    public void clearDb() {
        userRepository.deleteAll();
    }

    @Test
    public void canRegister() throws Exception {
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String username = fakerName.fullName();
        String email = fakerName.lastName() + "-" + UUID.randomUUID() + "@gmail.com";
        String password = faker.internet().password(5, 255);

        CreateUserRequest request = new CreateUserRequest(null, username, email, password);

        mockMvc
                .perform(post(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void canConfirmRegistration() throws Exception {
        Faker faker = new Faker();
        Name fakerName = faker.name();

        String username = fakerName.fullName();
        String email = fakerName.lastName() + "-" + UUID.randomUUID() + "@gmail.com";
        String password = faker.internet().password(5, 255);

        CreateUserRequest request = new CreateUserRequest(null, username, email, password);

        mockMvc
                .perform(post(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        String token = getValidToken(username);

        mockMvc
                .perform(put(REGISTRATION_PATH + "/confirm")
                        .param("token", token))
                .andExpect(status().isNoContent());
    }

    private String getValidToken(String email) {
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
}
