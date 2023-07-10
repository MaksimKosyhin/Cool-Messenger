package com.example.end.controller;

import com.example.end.domain.model.Chat;
import com.example.end.domain.model.ChatMember;
import com.example.end.domain.model.User;
import com.example.end.repository.ChatMemberRepository;
import com.example.end.repository.ChatRepository;
import com.example.end.repository.UserRepository;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
public class TestTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    private Faker faker = new Faker();

    @Test
    public void aaa() {
        userRepository.deleteAll();
        chatRepository.deleteAll();
        chatMemberRepository.deleteAll();
    }

    @Test
    public void test() {
        User user1 = getValidUser();
        User user2 = getValidUser();

        Chat dialogue = new Chat();
        dialogue.setType(Chat.ChatType.DIALOGUE);
        dialogue = chatRepository.save(dialogue);

        ChatMember m1 = new ChatMember();
        m1.setId(new ChatMember.ChatMemberId(dialogue.getId(), user1.getId()));
        chatMemberRepository.save(m1);

        ChatMember m2 = new ChatMember();
        m2.setId(new ChatMember.ChatMemberId(dialogue.getId(), user2.getId()));
        chatMemberRepository.save(m2);

        Chat chat = new Chat();
        chat.setTitle(faker.lorem().word());
        chat = chatRepository.save(chat);

        user1.getFolders().put("all", Set.of(dialogue.getId(), chat.getId()));

        userRepository.save(user1);
    }

    private User getValidUser() {
        User user = new User();
        user.setUsername(faker.name().name());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password(5, 20));
        user.setFolders(new HashMap<>(Map.of("all", new HashSet<>())));
        user = userRepository.save(user);
        return user;
    }
}
