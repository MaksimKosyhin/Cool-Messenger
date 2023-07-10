package com.example.end.repository;

import com.example.end.domain.model.ChatMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends MongoRepository<ChatMember, String> {

}
