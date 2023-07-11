package com.example.end.repository;

import com.example.end.domain.model.Chat;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends MongoRepository<Chat, ObjectId> {
    public boolean existsByIdentifier(String identifier);
}
