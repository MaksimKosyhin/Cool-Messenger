package com.example.end.domain.model;

import com.mongodb.DBRef;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dialogues")
@CompoundIndexes({
        @CompoundIndex(def = "{'user1.userId: 1, user2.userId: 1}", unique = true),
        @CompoundIndex(def = "{'user2.userId: 1, user1.userId: 1}", unique = true)
})
@Data
public class Dialogue {

    @Id
    private String id;

    private DBRef user1;
    private DBRef user2;
}
