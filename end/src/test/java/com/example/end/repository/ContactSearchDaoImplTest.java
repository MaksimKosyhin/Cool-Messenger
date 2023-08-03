package com.example.end.repository;

import com.example.end.domain.dto.*;
import com.example.end.domain.mapper.ChatViewMapper;
import com.example.end.domain.mapper.UserViewMapper;
import com.example.end.domain.model.Chat;
import com.example.end.domain.model.ChatMember;
import com.example.end.domain.model.User;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ComponentScan(
        basePackages = "com.example.end.domain.mapper",
        basePackageClasses = {ContactSearchDao.class, ContactSearchDaoImpl.class})
@Testcontainers
@ActiveProfiles("test")
public class ContactSearchDaoImplTest {
    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest")
            .withExposedPorts(27017)
            .waitingFor(new LogMessageWaitStrategy());

    private final Faker faker = new Faker();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatMemberRepository chatMemberRepository;
    @Autowired
    private ContactSearchDaoImpl contactSearchDao;
    @Autowired
    private UserViewMapper userViewMapper;
    @Autowired
    private ChatViewMapper chatViewMapper;

    @BeforeAll
    public static void open() {
        mongoDBContainer.start();
        var mappedPort = mongoDBContainer.getMappedPort(27017);
        System.setProperty("mongodb.container.port", String.valueOf(mappedPort));
    }

    @AfterEach
    public void deleteAll() {
        userRepository.deleteAll();
        chatRepository.deleteAll();
        chatMemberRepository.deleteAll();
    }

    @Test
    public void getPersonalContacts() {
        User user1 = getValidUser();
        user1 = userRepository.save(user1);
        User user2 = getValidUser();
        user2 = userRepository.save(user2);

        Chat dialogue = new Chat();
        dialogue.setType(Chat.ChatType.DIALOGUE);
        dialogue = chatRepository.save(dialogue);
        user1.getContacts().add(dialogue.getId());
        user2.getContacts().add(dialogue.getId());

        ChatMember m1 = new ChatMember();
        m1.setId(new ChatMember.ChatMemberId(dialogue.getId(), user1.getId()));
        m1.setPermissions(ChatMember.Permission.getAll());
        chatMemberRepository.save(m1);

        ChatMember m2 = new ChatMember();
        m2.setId(new ChatMember.ChatMemberId(dialogue.getId(), user2.getId()));
        m2.setPermissions(ChatMember.Permission.getAll());
        chatMemberRepository.save(m2);

        Chat chat = getValidChat();
        chat = chatRepository.save(chat);
        user1.getContacts().add(chat.getId());

        ChatMember m3 = new ChatMember();
        m3.setId(new ChatMember.ChatMemberId(chat.getId(), user1.getId()));
        m3.setPermissions(ChatMember.Permission.getAll());
        chatMemberRepository.save(m3);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        List<PersonalContact> result = contactSearchDao.getPersonalContacts(user1.getIdentifier());

        List<PersonalContact> expected = List.of(
                new PersonalContact(
                        dialogue.getId(),
                        user2.getDisplayName(),
                        user2.getIdentifier(),
                        user2.getImageUrl(),
                        user2.getInfo(),
                        m1.getPermissions()
                ),
                new PersonalContact(
                        chat.getId(),
                        chat.getDisplayName(),
                        chat.getIdentifier(),
                        chat.getImageUrl(),
                        chat.getInfo(),
                        m3.getPermissions()
                )
        );

        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void getChatMembers() {
        User user1 = getValidUser();
        user1 = userRepository.save(user1);
        User user2 = getValidUser();
        user2 = userRepository.save(user2);
        User user3 = getValidUser();
        user3 = userRepository.save(user3);
        User user4 = getValidUser();
        user4 = userRepository.save(user4);

        Chat chat = getValidChat();
        chat = chatRepository.save(chat);

        ChatMember m1 = new ChatMember();
        m1.setId(new ChatMember.ChatMemberId(chat.getId(), user1.getId()));
        m1 = chatMemberRepository.save(m1);

        ChatMember m2 = new ChatMember();
        m2.setId(new ChatMember.ChatMemberId(chat.getId(), user2.getId()));
        m2 = chatMemberRepository.save(m2);

        ChatMember m3 = new ChatMember();
        m3.setId(new ChatMember.ChatMemberId(chat.getId(), user3.getId()));
        m3 = chatMemberRepository.save(m3);

        ChatMember m4 = new ChatMember();
        m4.setId(new ChatMember.ChatMemberId(chat.getId(), user4.getId()));
        m4 = chatMemberRepository.save(m4);

        List<Contact> result = contactSearchDao.getChatMembers(chat.getId(), PageRequest.of(1, 2));

        List<Contact> expected = List.of(
                userViewMapper.toContact(user3),
                userViewMapper.toContact(user4)
        );

        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void searchChatMembers() {
        String prefix = "username";
        User user1 = getValidUser();
        user1.setIdentifier(prefix + user1.getIdentifier());
        user1 = userRepository.save(user1);
        User user2 = getValidUser();
        user2.setIdentifier(prefix + user2.getIdentifier());
        user2 = userRepository.save(user2);
        User user3 = getValidUser();
        user3 = userRepository.save(user3);

        Chat chat = getValidChat();
        chat = chatRepository.save(chat);

        ChatMember m1 = new ChatMember();
        m1.setId(new ChatMember.ChatMemberId(chat.getId(), user1.getId()));
        m1 = chatMemberRepository.save(m1);

        ChatMember m2 = new ChatMember();
        m2.setId(new ChatMember.ChatMemberId(chat.getId(), user2.getId()));
        m2 = chatMemberRepository.save(m2);

        ChatMember m3 = new ChatMember();
        m3.setId(new ChatMember.ChatMemberId(chat.getId(), user3.getId()));
        m3 = chatMemberRepository.save(m3);

        List<Contact> result = contactSearchDao.searchChatMembers(
                chat.getId(), new ChatMembersQuery(AllowedField.IDENTIFIER, prefix), PageRequest.of(0, 3));

        List<Contact> expected = List.of(
                userViewMapper.toContact(user1),
                userViewMapper.toContact(user2)
        );

        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void searchContacts() {
        String prefix = "username";
        User user1 = getValidUser();
        user1.setIdentifier(prefix + user1.getIdentifier());
        user1 = userRepository.save(user1);
        User user2 = getValidUser();
        user2.setIdentifier(prefix + user2.getIdentifier());
        user2 = userRepository.save(user2);
        User user3 = getValidUser();
        user3 = userRepository.save(user3);

        List<Contact> result = contactSearchDao.searchContacts(
                new ContactQuery(ContactCollection.USERS, AllowedField.IDENTIFIER, prefix), PageRequest.of(1, 1));

        List<Contact> expected = List.of(
                userViewMapper.toContact(user2)
        );

        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
    }

    private User getValidUser() {
        User user = new User();
        user.setDisplayName(faker.name().fullName());
        user.setIdentifier(faker.name().username());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password(5, 10));
        user.setEnabled(true);
        user.setContacts(new HashSet<>());
        user.setFolders(new HashMap<>());
        user.setRemainders(new HashSet<>());
        return user;
    }

    public Chat getValidChat() {
        Chat chat = new Chat();
        chat.setType(Chat.ChatType.GROUP);
        chat.setDisplayName(faker.company().name());
        chat.setInfo(faker.lorem().sentence());
        chat.setIdentifier(faker.name().username());
        chat.setType(Chat.ChatType.GROUP);
        chat.setImageUrl(faker.internet().url());
        return chat;
    }
}
