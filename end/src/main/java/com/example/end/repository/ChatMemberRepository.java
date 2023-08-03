package com.example.end.repository;

import com.example.end.domain.model.ChatMember;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends MongoRepository<ChatMember, ChatMember.ChatMemberId>, CustomChatMemberRepository {
}

interface CustomChatMemberRepository {
    public void deleteChatMembers(ObjectId chatId);
}

class CustomChatMemberRepositoryImpl implements CustomChatMemberRepository{

    private final MongoCollection<Document> chatMembers;

    @Autowired
    public CustomChatMemberRepositoryImpl(MongoTemplate mongoTemplate) {
        this.chatMembers = mongoTemplate.getCollection("chat-members");
    }

    @Override
    public void deleteChatMembers(ObjectId chatId) {
        chatMembers.deleteMany(new BasicDBObject("_id.chatId", chatId));
    }
}