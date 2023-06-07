package com.example.end.controller;

import com.example.end.config.MailConfig;
import com.example.end.domain.dto.AuthRequest;
import com.example.end.domain.dto.AuthResponse;
import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.domain.mapper.UserEditMapper;
import com.example.end.domain.mapper.UserViewMapper;
import com.example.end.domain.model.User;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
    private static final String REGISTRATION_PATH = "/api/v1/users";

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    private final MailConfig.MockMailSender mailSender;

    private final Faker faker = new Faker();

    private final UserRepository userRepository;

    private final UserViewMapper userViewMapper;

    private final UserEditMapper userEditMapper;

    private final JwtEncoder encoder;

    @Autowired
    public UserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, JavaMailSender mailSender, UserRepository userRepository, UserViewMapper userViewMapper, UserEditMapper userEditMapper, JwtEncoder encoder) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.mailSender = (MailConfig.MockMailSender) mailSender;
        this.userRepository = userRepository;
        this.userViewMapper = userViewMapper;
        this.userEditMapper = userEditMapper;
        this.encoder = encoder;
    }

    @AfterEach
    public void clearDb() {
        userRepository.deleteAll();
    }

    @Test
    void canLogin() throws Exception {

        User user = new User();
        user.setUsername(faker.name().name());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password(5, 20));
        user.setFolders(Map.of("all", List.of()));

        AuthRequest authRequest = new AuthRequest(user.getUsername(), user.getPassword());

        String unauthorizedResponse = mockMvc.perform(post(REGISTRATION_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> ex = objectMapper.readValue(unauthorizedResponse, HashMap.class);
        assertThat(ex).isEqualTo(Map.of("auth", "Wrong username or password"));

        CreateUserRequest createUserRequest = new CreateUserRequest(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getPassword());

        mockMvc.perform(post(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated());

        String token = mailSender.getToken();

        mockMvc.perform(put(REGISTRATION_PATH + "/confirm")
                        .param("token", token))
                .andExpect(status().isNoContent());

        String okResponse = mockMvc.perform(post(REGISTRATION_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse result = objectMapper.readValue(okResponse, AuthResponse.class);
        assertThat(result.token()).isNotNull();
        assertThat(result.user()).isEqualTo(userViewMapper.toLoggedInUser(user));
    }
}
