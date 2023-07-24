package com.example.end.repository;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.PersonalContact;
import com.example.end.domain.mapper.ChatViewMapper;
import com.example.end.domain.mapper.UserViewMapper;
import com.example.end.domain.model.Chat;
import com.example.end.domain.model.ChatMember;
import com.example.end.domain.model.User;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    private final Faker faker = new Faker();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatMemberRepository chatMemberRepository;
    @Autowired
    public ContactsSearchDao contactsSearchDao;

    @Autowired
    private UserViewMapper userViewMapper;

    @Autowired
    private ChatViewMapper chatViewMapper;

    @AfterEach
    public void deleteAll() {
        userRepository.deleteAll();
        chatRepository.deleteAll();
        chatMemberRepository.deleteAll();
    }

    @Test
    public void contactExists() {
        Chat chat = new Chat();
        chat.setTitle(faker.name().title());
        chat = chatRepository.save(chat);

        assertThat(contactsSearchDao.contactExists(chat.getId())).isTrue();
    }

    @Test
    public void getChatMembers() {
        User user1 = getValidUser();
        User user2 = getValidUser();

        Chat chat = new Chat();
        chat = chatRepository.save(chat);

        ChatMember m1 = new ChatMember();
        m1.setId(new ChatMember.ChatMemberId(chat.getId(), user1.getId()));
        chatMemberRepository.save(m1);

        ChatMember m2 = new ChatMember();
        m2.setId(new ChatMember.ChatMemberId(chat.getId(), user2.getId()));
        chatMemberRepository.save(m2);

        List<Contact> contacts = contactsSearchDao.getChatMembers(chat.getId(), PageRequest.of(0, 2));

        assertThat(contacts).isEqualTo(List.of(
                userViewMapper.toContact(user1),
                userViewMapper.toContact(user2)
        ));
    }

    @Test
    public void getContacts() {
        User user1 = getValidUser();
        User user2 = getValidUser();

        Chat chat = getValidChat();

        Chat dialogue = new Chat();
        dialogue.setType(Chat.ChatType.DIALOGUE);
        dialogue = chatRepository.save(dialogue);

        ChatMember m1 = new ChatMember();
        m1.setId(new ChatMember.ChatMemberId(dialogue.getId(), user1.getId()));
        m1.setPermissions(ChatMember.Permission.getAll());
        m1 = chatMemberRepository.save(m1);

        ChatMember m2 = new ChatMember();
        m2.setId(new ChatMember.ChatMemberId(dialogue.getId(), user2.getId()));
        m2.setPermissions(ChatMember.Permission.getAll());
        m2 = chatMemberRepository.save(m2);

        user1.getFolders().put("all", Set.of(chat.getId(), dialogue.getId()));
        user1 = userRepository.save(user1);

        List<PersonalContact> contacts = contactsSearchDao.getPersonalContacts(user1.getUsername());

        PersonalContact dialogueContact = new PersonalContact(
                dialogue.getId(),
                user2.getDisplayName(),
                user2.getUsername(),
                user2.getImageUrl(),
                user2.getInfo(),
                m1.getPermissions()
        );

        assertThat(contacts).containsExactlyInAnyOrder(
                chatViewMapper.toPersonalContact(chat, null),
                dialogueContact);
    }

    public Chat getValidChat() {
        Chat chat = new Chat();
        chat.setType(Chat.ChatType.GROUP);
        chat.setTitle(faker.company().name());
        chat.setInfo(faker.lorem().sentence());
        chat.setIdentifier(faker.name().username());
        chat.setType(Chat.ChatType.GROUP);
        chat.setImageUrl(faker.internet().url());
        return chatRepository.save(chat);
    }

    private User getValidUser() {
        User user = new User();
        user.setDisplayName(faker.name().fullName());
        user.setUsername(faker.name().username());
        user.setInfo(faker.lorem().sentence());
        user.setEmail(faker.internet().emailAddress());
        user.setImageUrl(faker.internet().url());
        user.setPassword(faker.internet().password(5, 20));
        user.setFolders(new HashMap<>(Map.of("all", new HashSet<>())));
        user = userRepository.save(user);
        return user;
    }
}