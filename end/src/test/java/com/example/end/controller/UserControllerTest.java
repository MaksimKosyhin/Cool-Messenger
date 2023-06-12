package com.example.end.controller;

import com.example.end.domain.dto.*;
import com.example.end.domain.mapper.UserViewMapper;
import com.example.end.domain.model.User;
import com.example.end.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.mongodb.DBRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    private final Faker faker = new Faker();

    private final UserRepository userRepository;

    private final UserViewMapper userViewMapper;

    private final JwtEncoder jwtEncoder;


    @Autowired
    public UserControllerTest(MockMvc mockMvc,
                              ObjectMapper objectMapper,
                              UserRepository userRepository,
                              UserViewMapper userViewMapper,
                              JwtEncoder jwtEncoder) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.userViewMapper = userViewMapper;
        this.jwtEncoder = jwtEncoder;
    }

    @AfterEach
    public void clearDb() {
        userRepository.deleteAll();
    }

    @Test
    void login_withUsername_ok() throws Exception {
        User user = getValidUser();
        registerUser(user);

        AuthRequest request = new AuthRequest(user.getUsername(), user.getPassword());

        String response = mockMvc.perform(post(REGISTRATION_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse result = objectMapper.readValue(response, AuthResponse.class);
        assertThat(result.token()).isNotEmpty();
        assertThat(result.user()).isEqualTo(userViewMapper.toLoggedInUser(user));
    }

    @Test
    void login_withEmail_ok() throws Exception {
        User user = getValidUser();
        registerUser(user);

        AuthRequest request = new AuthRequest(user.getEmail(), user.getPassword());

        String response = mockMvc.perform(post(REGISTRATION_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse result = objectMapper.readValue(response, AuthResponse.class);
        assertThat(result.token()).isNotEmpty();
        assertThat(result.user()).isEqualTo(userViewMapper.toLoggedInUser(user));
    }

    @Test
    public void login_withIncorrectCredentials_unauthorized() throws Exception {
        User user = getValidUser();
        registerUser(user);

        AuthRequest request = new AuthRequest(user.getUsername(), faker.internet().password());

        String response = mockMvc.perform(post(REGISTRATION_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Bad credentials");
    }

    @Test
    public void login_whenUserNotExists_unauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest(faker.name().username(), faker.internet().password());

        String response = mockMvc.perform(post(REGISTRATION_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("bad credentials");
    }

    @Test
    public void login_withoutAccountEnabled_forbidden() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                null,
                faker.name().username(),
                faker.internet().emailAddress(),
                faker.internet().password(5, 255)
        );

        mockMvc.perform(post(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)));

        AuthRequest authRequest = new AuthRequest(
                createUserRequest.username(),
                createUserRequest.password());

        String response = mockMvc.perform(post(REGISTRATION_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Confirm your account on email to get access to your profile");
    }

    @Test
    public void register_withInvalidCredentials_badRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                null,
                faker.internet().emailAddress(),
                "123",
                "1234");

        String response = mockMvc.perform(post(REGISTRATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> expected = Map.of(
                "email", "must be valid email",
                "password", "must be at least 5 characters long",
                "username", "username cannot be an email"
        );

        Map<String, String> result = objectMapper.readValue(response, HashMap.class);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void register_withValidCredentials_created() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                null,
                faker.name().username(),
                faker.internet().emailAddress(),
                faker.internet().password(5, 255)
        );

        mockMvc.perform(post(REGISTRATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(userRepository.existsByUsername(request.username())).isTrue();
    }

    @Test
    public void register_whenUsernameIsOccupied_conflict() throws Exception {
        User user = getValidUser();
        registerUser(user);

        CreateUserRequest request = new CreateUserRequest(
                null,
                user.getUsername(),
                faker.internet().emailAddress(),
                faker.internet().password(5, 255)
        );

        String response = mockMvc.perform(post(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("user with this username already exists");
    }

    @Test
    public void register_whenEmailIsOccupied_conflict() throws Exception {
        User user = getValidUser();
        registerUser(user);

        CreateUserRequest request = new CreateUserRequest(
                null,
                faker.name().username(),
                user.getEmail(),
                faker.internet().password(5, 255)
        );

        String response = mockMvc.perform(post(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("user with this email already exists");
    }

    @Test
    public void confirmRegistration_withValidToken_noContent() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                null,
                faker.name().username(),
                faker.internet().emailAddress(),
                faker.internet().password(5, 255)
        );

        mockMvc.perform(post(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        User user = userRepository.findByUsername(request.username()).get();
        String token = getValidConfirmationToken(user.getId(), user.getEmail());

        mockMvc.perform(put(REGISTRATION_PATH + "/confirm")
                .param("token", token))
                .andExpect(status().isNoContent());

        user = userRepository.findByUsername(request.username()).get();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    public void confirmRegistration_withExpiredToken_unauthorized() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                null,
                faker.name().username(),
                faker.internet().emailAddress(),
                faker.internet().password(5, 255)
        );

        mockMvc.perform(post(REGISTRATION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        User user = userRepository.findByUsername(request.username()).get();
        String token = getExpiredConfirmationToken(user.getId(), user.getEmail());

        String response = mockMvc.perform(put(REGISTRATION_PATH + "/confirm")
                        .param("token", token))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Confirmation token has expired. Try to send another confirmation");
    }

    @Test
    public void confirmRegistration_withInvalidToken_unauthorized() throws Exception {
        String token = "invalid token";

        String response = mockMvc.perform(put(REGISTRATION_PATH + "/confirm")
                        .param("token", token))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("invalid token");
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void updateUserInfo_withValidRequest_ok() throws Exception {
        User user1 = getValidUser();
        user1.setUsername("test-user");
        user1.setPassword("test-password");

        registerUser(user1);

        User user2 = getValidUser();
        registerUser(user2);
        user2 = userRepository.findByUsername(user2.getUsername()).get();

        User.Remainder remainder = new User.Remainder();
        remainder.setRef(new DBRef("users", user2.getId()));
        remainder.setMessage(faker.lorem().sentence());
        remainder.setNotifyAt(LocalDateTime.now());

        Set<DBRef> folders = Set.of(new DBRef("users", user2.getId()));

        UpdateUserRequest request = new UpdateUserRequest(
                faker.name().name(),
                faker.lorem().sentence(),
                Set.of(remainder),
                Map.of("all", folders)
        );

        String response = mockMvc.perform(put(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoggedInUser result = objectMapper.readValue(response, LoggedInUser.class);
        assertThat(result.info()).isEqualTo(request.info());
        assertThat(result.displayName()).isEqualTo(request.displayName());
        assertThat(result.folders()).isEqualTo(request.folders());
        assertThat(result.remainders()).isEqualTo(request.remainders());
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void updateUserInfo_withInValidRequest_bsdRequest() throws Exception {
        User user1 = getValidUser();
        user1.setUsername("test-user");
        user1.setPassword("test-password");

        registerUser(user1);

        User user2 = getValidUser();
        registerUser(user2);
        user2 = userRepository.findByUsername(user2.getUsername()).get();
        userRepository.delete(user2);

        User.Remainder remainder = new User.Remainder();
        remainder.setRef(new DBRef("users", user2.getId()));
        remainder.setMessage(faker.lorem().sentence());
        remainder.setNotifyAt(LocalDateTime.now());

        Set<DBRef> folders = Set.of(new DBRef("users", user2.getId()));

        UpdateUserRequest request = new UpdateUserRequest(
                faker.name().name(),
                faker.lorem().sentence(),
                Set.of(remainder),
                Map.of("all", folders)
        );

        String response = mockMvc.perform(put(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Nonexistent entity references were provided");
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void changePassword_withValidRequest_noContent() throws Exception {
        User user = getValidUser();
        user.setUsername("test-user");
        user.setPassword("test-password");

        registerUser(user);

        UpdatePasswordRequest request =
                new UpdatePasswordRequest(user.getPassword(), faker.internet().password());

        mockMvc.perform(put(REGISTRATION_PATH + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        user = userRepository.findByUsername(user.getUsername()).get();
        assertThat(user.getPassword()).isNotEqualTo(request.oldPassword());
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void changePassword_withWrongOldPassword_unauthorized() throws Exception {
        User user = getValidUser();
        user.setUsername("test-user");
        user.setPassword("test-password");

        registerUser(user);

        UpdatePasswordRequest request =
                new UpdatePasswordRequest(faker.internet().password(), faker.internet().password());

        String response = mockMvc.perform(put(REGISTRATION_PATH + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Bad credentials");
    }

    @Test
    @WithMockUser(username = "test-user", password = "test-password")
    public void changePassword_withInvalidNewPassword_badRequest() throws Exception {
        User user = getValidUser();
        user.setUsername("test-user");
        user.setPassword("test-password");

        registerUser(user);

        UpdatePasswordRequest request =
                new UpdatePasswordRequest(user.getPassword(), "");

        String response = mockMvc.perform(put(REGISTRATION_PATH + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> msg = objectMapper.readValue(response, HashMap.class);

        assertThat(msg).isEqualTo(Map.of("newPassword", "should be at least 5 characters long"));
    }

    private void registerUser(User user) throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getPassword());

        mockMvc.perform(post(REGISTRATION_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        user = userRepository.findByUsername(user.getUsername()).get();
        String token = getValidConfirmationToken(user.getId(), user.getEmail());

        mockMvc.perform(put(REGISTRATION_PATH + "/confirm")
                        .param("token", token));
    }

    private User getValidUser() {
        User user = new User();
        user.setUsername(faker.name().name());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password(5, 20));
        user.setFolders(Map.of("all", Set.of()));
        return user;
    }

    private String getValidConfirmationToken(String userId, String email) {
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

    private String getExpiredConfirmationToken(String userId, String email) {
        var expiry = 60 * 60 * 24;
        var now = Instant.now().minus(expiry, ChronoUnit.SECONDS);

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
}
