package com.example.end.repository;

import com.example.end.domain.model.Dialogue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DialogueRepository extends MongoRepository<Dialogue, String> {
}
