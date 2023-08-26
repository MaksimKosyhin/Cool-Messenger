package com.example.end.repository;

import com.example.end.domain.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    public boolean existsByEmail(String email);

    public boolean existsByIdentifier(String identifier);
    public Optional<User> findByIdentifier(String identifier);

    public Optional<User> findByEmail(String email);
}