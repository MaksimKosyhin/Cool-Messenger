package com.example.end.repository;

import com.example.end.domain.dto.Contact;
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
    public ContactsDao contactsDao;

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

        assertThat(contactsDao.contactExists(chat.getId())).isTrue();
    }

    @Test
    public void addContact() {
        User user = getValidUser();

        Chat chat = new Chat();
        chat.setTitle(faker.name().title());
        chat = chatRepository.save(chat);

        contactsDao.addContact(user.getUsername(), chat.getId());

        user = userRepository.findById(user.getId().toString()).get();

        assertThat(user.getFolders().get("all")).contains(chat.getId());
    }

    @Test
    public void removeContact() {
        User user = getValidUser();

        Chat chat = new Chat();
        chat.setTitle(faker.name().title());
        chat = chatRepository.save(chat);

        user.getFolders().get("all").add(chat.getId());
        user = userRepository.save(user);

        contactsDao.removeContact(user.getUsername(), chat.getId());
        user = userRepository.findById(user.getId().toString()).get();

        assertThat(user.getFolders().get("all").contains(chat.getId())).isFalse();
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

        List<Contact> contacts = contactsDao.getChatMembers(chat.getId(), PageRequest.of(0, 2));

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
        m1 = chatMemberRepository.save(m1);

        ChatMember m2 = new ChatMember();
        m2.setId(new ChatMember.ChatMemberId(dialogue.getId(), user2.getId()));
        m2 = chatMemberRepository.save(m2);

        user1.getFolders().put("all", Set.of(chat.getId(), dialogue.getId()));
        user1 = userRepository.save(user1);

        List<Contact> contacts = contactsDao.getContacts(user1.getUsername());

        Contact dialogueView = new Contact(
                dialogue.getId(),
                user2.getDisplayName(),
                user2.getUsername(),
                user2.getImageUrl(),
                user2.getInfo());

        assertThat(contacts).containsExactlyInAnyOrder(
                dialogueView,
                chatViewMapper.toContact(chat));
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