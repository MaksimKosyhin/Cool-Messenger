package com.example.end.controller;

import com.example.end.domain.model.Chat;
import com.example.end.domain.model.ChatMembers;
import com.example.end.domain.model.EntityReference;
import com.example.end.domain.model.User;
import com.example.end.repository.ChatMembersRepository;
import com.example.end.repository.ChatRepository;
import com.example.end.repository.UserRepository;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

@SpringBootTest
@ActiveProfiles("test")
public class TestTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMembersRepository chatMembersRepository;

    private Faker faker = new Faker();

//    @AfterEach
//    public void aaa() {
//        userRepository.deleteAll();
//        chatRepository.deleteAll();
//        chatMembersRepository.deleteAll();
//    }

    @Test
    public void test() {
        User user1 = getValidUser();
        User user2 = getValidUser();

        Chat dialogue = new Chat();
        dialogue.setDialogue(true);
        dialogue = chatRepository.save(dialogue);
        ChatMembers members = new ChatMembers();
        members.setChatId(dialogue.getId());

        ChatMembers.Member member1 = new ChatMembers.Member();
        member1.setUserId(user1.getId());

        ChatMembers.Member member2 = new ChatMembers.Member();
        member2.setUserId(user2.getId());

        members.setMembers(new HashSet<>(List.of(member1, member2)));
        chatMembersRepository.save(members);

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
