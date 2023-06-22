package com.example.end.repository;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.model.EntityReference;
import com.mongodb.DBRef;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ContactsDaoImpl implements ContactsDao {

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean chatExists(String id) {
        return mongoTemplate.exists(
                Query.query(Criteria.where("_id")
                        .is(id)), "chats");
    }
//todo: use pipeline in lookup
//db.users.aggregate([
//    {$match: {_id: ObjectId("6493145acee9947b8722ff7d")}},
//    {$project: {_id: 0, "folders.all": {$filter: {input: "$folders.all", as: "ref", cond: {$strcasecmp: ["$ref.collectionName", "dialogues"]}}}}},
//    {$lookup: {from: "dialogues", localField: "folders.all._id", foreignField: "_id", as: "dialogue"}},
//    {$unwind: "$dialogue"},
//    {$project: {_id: "$dialogue.id", user: {$cond: {if: {_id: "$dialogue.user1"}, then: "$dialogue.user1", else: "$dialogue.user2"}}},
//        {$lookup: {from: "users", localField: "user", foreignField: "_id", as: "user"}},
//        {$project: {_id: 0, ref: {_id: "_id", collectionName: "dialogues"}, displayName: "$user.displayName", identifier: "$user.username", imageUrl: "$user.imageUrl", info: "$user.info" }}
//])

//    db.users.aggregate([
//    {$match: {_id: ObjectId("649fef9ee1ce874005650a4b")}},
//    {$project: {_id: 0, "chats": {$filter: {input: "$folders.all", as: "ref", cond: {$eq: ["$$ref.collectionName", "chats"]}}}}},
//    {$lookup: {from: "chats", localField: "chats._id", foreignField: "_id", as: "chat"}},
//    {$unwind: "$chat"},
//    {$project: { _id: 0, ref: {_id: "$chat._id", collectionName: "chats"}, displayName: "$chat.title", identifier: "$chat.identifier", imageUrl: "$chat.imageUrl", info: "$chat.info" }}])
    @Override
    public Set<Contact> getContacts(String username, Pageable page) {
        MatchOperation match = Aggregation.match(Criteria.where("username").is(username));

        var project = Aggregation.project()
                .and(
                        ConditionalOperators.Cond
                                .when(
                                        ComparisonOperators.Eq.valueOf("$$ref.collectionName").equalToValue("chats")
                                )
                                .thenValueOf("$$ref")
                                .otherwise(null)
                )
                .as("chats");

        var lookup = Aggregation.lookup("chats", "chats._id", "_id", "chat");

        var unwind = Aggregation.unwind("$chat");

        var finalProject = Aggregation.project()
                .andExclude("_id")
                .andExpression("chat._id").as("ref._id")
                .andExpression("chats").as("ref._collectionName")
                .and("chat.title").as("displayName")
                .and("chat.identifier").as("identifier")
                .and("chat.imageUrl").as("imageUrl")
                .and("chat.info").as("info");

        Aggregation aggregation = Aggregation.newAggregation(match, project, lookup, unwind, finalProject);
        AggregationResults<Contact> result = mongoTemplate.aggregate(aggregation, "users", Contact.class);

        return result.getMappedResults().stream().collect(Collectors.toSet());
    }

    @Override
    public String joinChat(String username, EntityReference ref) {
        return null;
    }

    @Override
    public String leaveChat(String username, String id) {
        return null;
    }
}
