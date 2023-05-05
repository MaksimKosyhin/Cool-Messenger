package com.example.end.repository;

import com.example.end.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    public boolean existsByEmail(String email);
    public Optional<User> findByEmail(String email);
}