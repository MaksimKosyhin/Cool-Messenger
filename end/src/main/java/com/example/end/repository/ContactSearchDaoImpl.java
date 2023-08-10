package com.example.end.repository;

import com.example.end.domain.dto.ChatMembersQuery;
import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.ContactQuery;
import com.example.end.domain.dto.PersonalContact;
import com.example.end.domain.model.Chat;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
public class ContactSearchDaoImpl implements ContactSearchDao {

    private final MongoDatabase db;

    @Autowired
    public ContactSearchDaoImpl(MongoTemplate mongoTemplate) {
        this.db = mongoTemplate.getDb();
    }

    @Override
    public List<PersonalContact> getPersonalContacts(String identifier) {
        var matchUser = new BasicDBObject("$match", new BasicDBObject("identifier", identifier));

        var lookupChats = new BasicDBObject("$lookup",
                new BasicDBObject(Map.of(
                        "from", "chats",
                        "localField", "contacts",
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

        var lookupPermissions = lookupPermission();

        var unwindUser = new BasicDBObject("$unwind",
                new BasicDBObject(Map.of(
                        "path", "$user",
                        "preserveNullAndEmptyArrays", true
                )));

        var projectContacts = projectPersonalContacts();


        var stages = List.of(
                matchUser,
                lookupChats,
                projectChat,
                unwindChat,
                lookupChatMembers,
                lookupUsers,
                lookupPermissions,
                unwindUser,
                projectContacts);

        var users = db.getCollection("users");
        List<PersonalContact> result = new LinkedList<>();
        users.aggregate(stages, PersonalContact.class).forEach(result::add);
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

    private BasicDBObject lookupPermission() {
        var let = new BasicDBObject(Map.of(
                "userId", "$_id",
                "chatId", "$chat._id"));

        var match = new BasicDBObject("$match",
                new BasicDBObject("$expr",
                        new BasicDBObject("$and", List.of(
                                new BasicDBObject("$eq", List.of("$$chatId", "$_id.chatId")),
                                new BasicDBObject("$eq", List.of("$$userId", "$_id.userId"))))));


        var lookup = new BasicDBObject("$lookup", new BasicDBObject(Map.of(
                "from", "chat-members",
                "let", let,
                "pipeline", Collections.singletonList(match),
                "as", "permissions"
        )));

        return lookup;
    }

    private BasicDBObject projectPersonalContacts() {
        var displayName = new BasicDBObject("$ifNull", List.of(
                "$user.displayName",
                "$chat.displayName"));

        var identifier = new BasicDBObject("$ifNull", List.of(
                "$user.identifier",
                "$chat.identifier"));

        var imagePath = new BasicDBObject("$ifNull", List.of(
                "$user.imagePath",
                "$chat.imagePath"));

        var info = new BasicDBObject("$ifNull", List.of(
                "$user.info",
                "$chat.info"));

        var permissions = new BasicDBObject("$getField",
                new BasicDBObject(Map.of(
                        "input", new BasicDBObject("$arrayElemAt", List.of("$permissions", 0)),
                        "field", "permissions")));

        var projectContacts = new BasicDBObject("$project", Map.of(
                "id", "$chat._id",
                "displayName", displayName,
                "identifier", identifier,
                "imagePath", imagePath,
                "info", info,
                "permissions", permissions
        ));

        return projectContacts;
    }

    @Override
    public List<Contact> getChatMembers(ObjectId chatId, Pageable pageable) {
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
                "identifier", 1,
                "imagePath", 1,
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

    @Override
    public List<Contact> searchChatMembers(ObjectId chatId, ChatMembersQuery query, Pageable pageable) {
        var matchChat = new BasicDBObject("$match", new BasicDBObject("_id.chatId", chatId));

        var lookupUsers = new BasicDBObject("$lookup",
                new BasicDBObject(Map.of(
                        "from", "users",
                        "localField", "_id.userId",
                        "foreignField", "_id",
                        "as", "member"
                )));

        var replaceRoot = new BasicDBObject("$replaceRoot", new BasicDBObject(
                new BasicDBObject("newRoot", new BasicDBObject(
                        "$arrayElemAt", List.of("$member", 0)))));

        var matchField = new BasicDBObject("$match",
                new BasicDBObject(query.field().fieldName,
                        new BasicDBObject(Map.of(
                                "$regex", query.value(),
                                "$options", "i"
                        ))));

        var skip = new BasicDBObject("$skip", pageable.getOffset());

        var limit = new BasicDBObject("$limit", pageable.getPageSize());

        var projectContacts = new BasicDBObject("$project", Map.of(
                "id", "$_id",
                "displayName", 1,
                "identifier", 1,
                "imagePath", 1,
                "info", 1
        ));

        var stages = List.of(
                matchChat,
                lookupUsers,
                replaceRoot,
                matchField,
                skip,
                limit,
                projectContacts
        );

        var chatMembers = db.getCollection("chat-members");
        List<Contact> result = new LinkedList<>();
        chatMembers.aggregate(stages, Contact.class).forEach(result::add);
        return result;
    }

    @Override
    public List<Contact> searchContacts(ContactQuery query, Pageable pageable) {
        var matchField = new BasicDBObject("$match",
                new BasicDBObject(query.field().fieldName,
                        new BasicDBObject(Map.of(
                                "$regex", query.value(),
                                "$options", "i"
                        ))));

        var skip = new BasicDBObject("$skip", pageable.getOffset());

        var limit = new BasicDBObject("$limit", pageable.getPageSize());

        var project = new BasicDBObject("$project", Map.of(
                "id", "$_id",
                "displayName", 1,
                "identifier", 1,
                "imagePath", 1,
                "info", 1
        ));

        var stages = List.of(
                matchField,
                skip,
                limit,
                project
        );

        var contacts = db.getCollection(query.collectionName().collectionName);
        List<Contact> result = new LinkedList<>();
        contacts.aggregate(stages, Contact.class).forEach(result::add);
        return result;
    }
}
