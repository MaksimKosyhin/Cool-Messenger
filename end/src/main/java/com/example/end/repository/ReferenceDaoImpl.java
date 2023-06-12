package com.example.end.repository;

import com.mongodb.DBRef;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReferenceDaoImpl implements ReferenceDao{

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean existsInDatabase(DBRef dbRef) {
        return mongoTemplate.exists(Query.query(Criteria.where("_id").is(dbRef.getId())), dbRef.getCollectionName());
    }
}
