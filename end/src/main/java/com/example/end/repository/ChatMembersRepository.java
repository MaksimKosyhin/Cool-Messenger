package com.example.end.repository;

import com.example.end.domain.model.ChatMembers;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMembersRepository extends MongoRepository<ChatMembers, String> {

}
