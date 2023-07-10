package com.example.end.repository;

import com.example.end.config.DbConfig;
import com.example.end.domain.dto.Contact;
import com.example.end.domain.model.Chat;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
public class ContactsDaoImpl implements ContactsDao {

    private final MongoDatabase db;

    @Autowired
    public ContactsDaoImpl(DbConfig config) {
        var client = config.mongoClient();
        this.db = client.getDatabase("cool-chat");
    }

    @Override
    public List<Contact> getContacts(String username) {
        var matchUser = new BasicDBObject("$match", new BasicDBObject("username", username));

        var lookupChats = new BasicDBObject("$lookup",
                new BasicDBObject(Map.of(
                        "from", "chats",
                        "localField", "folders.all",
                        "foreignField", "_id",
                        "as", "chat")));

        var projectChat = new BasicDBObject("$project", new BasicDBObject("chat", 1));

        var unwindChat = new BasicDBObject("$unwind", new BasicDBObject("path", "$chat"));

        var lookupChatMembers = lookupChatMembers();

        var lookupUsers = new BasicDBObject("$lookup",
                new BasicDBObject(Map.of(
                        "from", "users",
                        "localField", "members._id.userId",
                        "foreignField", "_id",
                        "as", "user")));

        var unwindUser = new BasicDBObject("$unwind",
                new BasicDBObject(Map.of(
                        "path", "$user",
                        "preserveNullAndEmptyArrays", true
                )));

        var projectContacts = projectContacts();


        var stages = List.of(
                matchUser,
                lookupChats,
                projectChat,
                unwindChat,
                lookupChatMembers,
                lookupUsers,
                unwindUser,
                projectContacts);

        var users = db.getCollection("users");
        List<Contact> result = new LinkedList<>();
        users.aggregate(stages, Contact.class).forEach(result::add);
        return result;
    }

    private BasicDBObject lookupChatMembers() {
        var let = new BasicDBObject(Map.of(
                "userId", "$_id",
                "chatId", "$chat._id",
                "type", "$chat.type"));

        var match = new BasicDBObject("$match",
                new BasicDBObject("$expr",
                        new BasicDBObject("$and", List.of(
                                new BasicDBObject("$eq", List.of("$$type", Chat.ChatType.DIALOGUE)),
                                new BasicDBObject("$eq", List.of("$$chatId", "$_id.chatId")),
                                new BasicDBObject("$ne", List.of("$$userId", "$_id.userId"))))));


        var lookup = new BasicDBObject("$lookup", new BasicDBObject(Map.of(
                "from", "chat-members",
                "let", let,
                "pipeline", Collections.singletonList(match),
                "as", "members"
        )));

        return lookup;
    }

    private BasicDBObject projectContacts() {
        var displayName = new BasicDBObject("$ifNull", List.of(
                "$user.displayName",
                "$chat.title"));

        var identifier = new BasicDBObject("$ifNull", List.of(
                "$user.username",
                "$chat.identifier"));

        var imageUrl = new BasicDBObject("$ifNull", List.of(
                "$user.imageUrl",
                "$chat.imageUrl"));

        var info = new BasicDBObject("$ifNull", List.of(
                "$user.info",
                "$chat.info"));

        var projectContacts = new BasicDBObject("$project", Map.of(
                "id", "$chat._id",
                "displayName", displayName,
                "identifier", identifier,
                "imageUrl", imageUrl,
                "info", info
        ));

        return projectContacts;
    }

    @Override
    public boolean contactExists(ObjectId chatId) {
        var chats = db.getCollection("chats");
        return chats.countDocuments(new BasicDBObject("_id", chatId)) == 1;
    }

    @Override
    public boolean addContact(String username, ObjectId chatId) {
        var users = db.getCollection("users");
        var result =  users.updateOne(
                new BasicDBObject("username", username),
                new BasicDBObject("$push", new BasicDBObject("folders.all", chatId)));
        return result.wasAcknowledged();
    }

    @Override
    public boolean removeContact(String username, ObjectId chatId) {
        var users = db.getCollection("users");
        var result =  users.updateOne(
                new BasicDBObject("username", username),
                new BasicDBObject("$pull", new BasicDBObject("folders.all", chatId)));
        return result.wasAcknowledged();
    }

    @Override
    public List<Contact> getChatMembers(ObjectId chatId, Pageable pageable) {
        Page a;
        var matchChat = new BasicDBObject("$match", new BasicDBObject("_id.chatId", chatId));

        var skip = new BasicDBObject("$skip", pageable.getOffset());

        var limit = new BasicDBObject("$limit", pageable.getPageSize());

        var lookupUsers = new BasicDBObject("$lookup",
                new BasicDBObject(Map.of(
                        "from", "users",
                        "localField", "_id.userId",
                        "foreignField", "_id",
                        "as", "user")));

        var replaceRoot = new BasicDBObject("$replaceRoot",
                new BasicDBObject("newRoot",
                        new BasicDBObject("$arrayElemAt", List.of("$user", 0))));

        var projectContacts = new BasicDBObject("$project", Map.of(
                "id", "$_id",
                "displayName", 1,
                "identifier", "$username",
                "imageUrl", 1,
                "info", 1
        ));

        var stages = List.of(
                matchChat,
                skip,
                limit,
                lookupUsers,
                replaceRoot,
                projectContacts
        );

        var chatMembers = db.getCollection("chat-members");
        List<Contact> result = new LinkedList<>();
        chatMembers.aggregate(stages, Contact.class).forEach(result::add);
        return result;
    }
}
