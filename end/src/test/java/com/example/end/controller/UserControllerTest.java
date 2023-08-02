package com.example.end.controller;

import com.example.end.config.TokenResolver;
import com.example.end.domain.dto.*;
import com.example.end.domain.mapper.UserEditMapper;
import com.example.end.domain.mapper.UserViewMapper;
import com.example.end.domain.model.User;
import com.example.end.repository.UserRepository;
import com.example.end.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    private static final String STARTING_PATH = "/api/v1/users";
    private final Faker faker = new Faker();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ChatService chatService;

    @MockBean
    private TokenResolver tokenResolver;

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserEditMapper userEditMapper;

    @Autowired
    private UserViewMapper userViewMapper;

    @Autowired
    private PasswordEncoder encoder;

    @Test
    public void register_validRequest_created() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                faker.name().fullName(),
                faker.name().username(),
                faker.internet().emailAddress(),
                faker.internet().password(5, 255)
        );

        ObjectId id = new ObjectId();

        Mockito.when(chatService.existsByIdentifier(request.username())).thenReturn(false);
        Mockito.when(userRepository.existsByEmail(request.email())).thenReturn(false);
        Mockito.when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(i -> {
            User saved = (User) i.getArguments()[0];
            saved.setId(id);
            return saved;
        });

        Mockito.when(tokenResolver.generateToken(id.toHexString(),
                Map.of("email", request.email()))).thenReturn("token");

        mockMvc.perform(post(STARTING_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void register_invalidRequest_badRequest() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                null,
                faker.internet().emailAddress(),
                "123",
                "1234");

        String response = mockMvc.perform(post(STARTING_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> expected = Map.of(
                "displayName", "display name must not be blank",
                "email", "must be valid email",
                "password", "must be at least 5 characters long",
                "username", "username cannot be an email"
        );

        Map<String, String> result = objectMapper.readValue(response, HashMap.class);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void register_usernameOccupied_conflict() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                faker.name().fullName(),
                faker.name().username(),
                faker.internet().emailAddress(),
                faker.internet().password(5, 255)
        );

        Mockito.when(chatService.existsByIdentifier(request.username())).thenReturn(true);

        String response = mockMvc.perform(post(STARTING_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo(String.format("username: %s is already occupied", request.username()));
    }

    @Test
    public void register_emailOccupied_conflict() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                faker.name().fullName(),
                faker.name().username(),
                faker.internet().emailAddress(),
                faker.internet().password(5, 255)
        );

        Mockito.when(userRepository.existsByEmail(request.email())).thenReturn(true);

        String response = mockMvc.perform(post(STARTING_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("user with this email already exists");
    }

    @Test
    public void login_userNotExists_unauthorized() throws Exception {
        AuthRequest request = new AuthRequest(faker.name().username(), faker.internet().password(5, 10));

        String response = mockMvc.perform(post(STARTING_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Bad credentials");
    }

    @Test
    public void login_wrongPassword_unauthorized() throws Exception {
        String password = faker.internet().password(5, 10);
        User user = getValidUser(password);

        AuthRequest request = new AuthRequest(user.getUsername(), "");

        Mockito.when(userRepository.existsByUsername(request.identifier())).thenReturn(true);
        Mockito.when(userRepository.findByUsername(request.identifier())).thenReturn(Optional.of(user));

        String response = mockMvc.perform(post(STARTING_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Bad credentials");
    }

    @Test
    public void login_withoutEnabledAccount_forbidden() throws Exception {
        String password = faker.internet().password(5, 10);
        User user = getValidUser(password);
        user.setEnabled(false);

        AuthRequest request = new AuthRequest(user.getUsername(), password);

        Mockito.when(userRepository.existsByUsername(request.identifier())).thenReturn(true);
        Mockito.when(userRepository.findByUsername(request.identifier())).thenReturn(Optional.of(user));

        String response = mockMvc.perform(post(STARTING_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Confirm your account on email to get access to your profile");
    }

    @Test
    public void login_withUsername_ok() throws Exception {
        String password = faker.internet().password(5, 10);
        User user = getValidUser(password);

        AuthRequest request = new AuthRequest(user.getUsername(), password);
        String token = "token";

        Mockito.when(userRepository.existsByUsername(request.identifier())).thenReturn(true);
        Mockito.when(userRepository.findByUsername(request.identifier())).thenReturn(Optional.of(user));
        Mockito.when(tokenResolver.generateToken(user.getId().toHexString())).thenReturn(token);

        String response = mockMvc.perform(post(STARTING_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse result = objectMapper.readValue(response, AuthResponse.class);
        assertThat(result.token()).isEqualTo(token);
        assertThat(result.user()).isEqualTo(userViewMapper.toLoggedInUser(user));
    }

    @Test
    public void login_withEmail_ok() throws Exception {
        String password = faker.internet().password(5, 10);
        User user = getValidUser(password);

        AuthRequest request = new AuthRequest(user.getEmail(), password);
        String token = "token";

        Mockito.when(userRepository.existsByEmail(request.identifier())).thenReturn(true);
        Mockito.when(userRepository.findByEmail(request.identifier())).thenReturn(Optional.of(user));
        Mockito.when(tokenResolver.generateToken(user.getId().toHexString())).thenReturn(token);

        String response = mockMvc.perform(post(STARTING_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse result = objectMapper.readValue(response, AuthResponse.class);
        assertThat(result.token()).isEqualTo(token);
        assertThat(result.user()).isEqualTo(userViewMapper.toLoggedInUser(user));
    }

    @Test
    public void login_withId_ok() throws Exception {
        String password = faker.internet().password(5, 10);
        User user = getValidUser(password);

        AuthRequest request = new AuthRequest(user.getId().toHexString(), password);
        String token = "token";

        Mockito.when(userRepository.existsById(new ObjectId(request.identifier()))).thenReturn(true);
        Mockito.when(userRepository.findById(new ObjectId(request.identifier()))).thenReturn(Optional.of(user));
        Mockito.when(tokenResolver.generateToken(user.getId().toHexString())).thenReturn(token);

        String response = mockMvc.perform(post(STARTING_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse result = objectMapper.readValue(response, AuthResponse.class);
        assertThat(result.token()).isEqualTo(token);
        assertThat(result.user()).isEqualTo(userViewMapper.toLoggedInUser(user));
    }

    @Test
    @WithMockUser(username = "64c91dc80d07a36a9a384efc")
    public void updateUserInfo_validRequest_ok() throws Exception {
        User user = getValidUser(new ObjectId("64c91dc80d07a36a9a384efc"));

        UpdateUserRequest request = new UpdateUserRequest(
                faker.name().username(),
                faker.name().fullName(),
                faker.lorem().sentence(),
                Collections.emptyMap(),
                Collections.emptySet()
        );

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);

        String response = mockMvc.perform(put(STARTING_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoggedInUser result = objectMapper.readValue(response, LoggedInUser.class);

        userEditMapper.update(request, user);
        assertThat(result).isEqualTo(userViewMapper.toLoggedInUser(user));
    }

    @Test
    @WithMockUser(username = "64c91dc80d07a36a9a384efc")
    public void updateUserInfo_invalidRequest_badRequest() throws Exception {
        User user = getValidUser(new ObjectId("64c91dc80d07a36a9a384efc"));

        UpdateUserRequest request = new UpdateUserRequest(
                faker.internet().emailAddress(),
                null,
                null,
                null,
                null
        );

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        String response = mockMvc.perform(put(STARTING_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> expected = Map.of(
                "displayName", "display name must not be blank",
                "username", "username cannot be an email"
        );

        Map<String, String> result = objectMapper.readValue(response, HashMap.class);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "64c91dc80d07a36a9a384efc")
    public void updateUserInfo_invalidContactReferences_badRequest() throws Exception {
        User user = getValidUser(new ObjectId("64c91dc80d07a36a9a384efc"));

        UpdateUserRequest request = new UpdateUserRequest(
                faker.name().username(),
                faker.name().fullName(),
                faker.lorem().sentence(),
                Map.of("all", Set.of(new ObjectId())),
                Collections.emptySet()
        );

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        String response = mockMvc.perform(put(STARTING_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("invalid contact references");
    }

    @Test
    @WithMockUser(username = "64c91dc80d07a36a9a384efc")
    public void removeContacts_validRequest_ok() throws Exception {
        User user = getValidUser(new ObjectId("64c91dc80d07a36a9a384efc"));
        ObjectId id1 = new ObjectId();
        ObjectId id2 = new ObjectId();

        user.getContacts().addAll(Set.of(id1, id2));
        user.getFolders().putAll(Map.of("one", new HashSet<>(Set.of(id1, id2)),
                                "two", new HashSet<>(Set.of(id2))));
        user.getRemainders().addAll(Set.of(new User.Remainder(id2, null, null)));

        Set<ObjectId> request = Set.of(id2);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(user)).thenAnswer(i -> i.getArguments()[0]);

        String response = mockMvc.perform(put(STARTING_PATH + "/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoggedInUser result = objectMapper.readValue(response, LoggedInUser.class);

        assertThat(result.contacts()).isEqualTo(Set.of(id1));
        assertThat(result.folders()).isEqualTo(Map.of("one", Set.of(id1), "two", Set.of()));
        assertThat(result.remainders()).isEqualTo(Set.of());
    }

    @Test
    @WithMockUser(username = "64c91dc80d07a36a9a384efc", password = "password")
    public void changePassword_validRequest_noContent() throws Exception {
        String password = "password";
        User user = getValidUser(new ObjectId("64c91dc80d07a36a9a384efc"), password);

        Mockito.when(userRepository.existsById(user.getId())).thenReturn(true);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UpdatePasswordRequest request =
                new UpdatePasswordRequest(password, faker.internet().password(5, 10));

        mockMvc.perform(put(STARTING_PATH + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "64c91dc80d07a36a9a384efc", password = "password")
    public void changePassword_incorrectOldPassword_unauthorized() throws Exception {
        String password = "password";
        User user = getValidUser(new ObjectId("64c91dc80d07a36a9a384efc"), password);

        Mockito.when(userRepository.existsById(user.getId())).thenReturn(true);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UpdatePasswordRequest request =
                new UpdatePasswordRequest("wrong-password", faker.internet().password(5, 10));

        String response = mockMvc.perform(put(STARTING_PATH + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).isEqualTo("Bad credentials");
    }

    @Test
    @WithMockUser(username = "64c91dc80d07a36a9a384efc", password = "password")
    public void changePassword_invalidNewPassword_badRequest() throws Exception {
        String password = "password";
        User user = getValidUser(new ObjectId("64c91dc80d07a36a9a384efc"), password);

        Mockito.when(userRepository.existsById(user.getId())).thenReturn(true);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UpdatePasswordRequest request =
                new UpdatePasswordRequest(password, "");

        String response = mockMvc.perform(put(STARTING_PATH + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> expected = Map.of(
                "newPassword", "should be at least 5 characters long"
        );

        Map<String, String> result = objectMapper.readValue(response, HashMap.class);
        assertThat(result).isEqualTo(expected);
    }

    private User getValidUser(ObjectId id, String password) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(faker.name().fullName());
        user.setUsername(faker.name().username());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(encoder.encode(password));
        user.setEnabled(true);
        user.setContacts(new HashSet<>());
        user.setFolders(new HashMap<>());
        user.setRemainders(new HashSet<>());
        return user;
    }

    private User getValidUser(ObjectId id) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(faker.name().fullName());
        user.setUsername(faker.name().username());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(encoder.encode(faker.internet().password(5, 10)));
        user.setEnabled(true);
        user.setContacts(new HashSet<>());
        user.setFolders(new HashMap<>());
        user.setRemainders(new HashSet<>());
        return user;
    }

    private User getValidUser(String password) {
        User user = new User();
        user.setId(new ObjectId());
        user.setDisplayName(faker.name().fullName());
        user.setUsername(faker.name().username());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(encoder.encode(password));
        user.setEnabled(true);
        user.setContacts(new HashSet<>());
        user.setFolders(new HashMap<>());
        user.setRemainders(new HashSet<>());
        return user;
    }
}
