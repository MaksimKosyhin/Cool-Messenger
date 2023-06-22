package com.example.end.repository;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    public boolean existsByEmail(String email);

    public boolean existsByUsername(String username);
    public Optional<User> findByUsername(String username);

    public Optional<User> findByEmail(String email);
}