package com.example.end.repository;

import com.example.end.domain.model.PublicChat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicChatRepository extends MongoRepository<PublicChat, String> {
}
