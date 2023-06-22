package com.example.end.repository;

import com.example.end.domain.model.User;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private Faker faker = new Faker();

    @AfterEach
    public void deleteAll() {
        userRepository.deleteAll();
    }

    private User saveValidUser() {
        User user = new User();
        user.setUsername(faker.name().name());
        user.setEmail(faker.internet().emailAddress());
        user.setPassword(faker.internet().password(5, 20));
        user.setFolders(new HashMap<>(Map.of("all", new HashSet<>())));
        user = userRepository.save(user);
        return user;
    }
}
