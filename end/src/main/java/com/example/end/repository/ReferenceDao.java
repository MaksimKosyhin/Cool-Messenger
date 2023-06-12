package com.example.end.repository;

import com.mongodb.DBRef;

//todo: write method to get View of user and chat items
public interface ReferenceDao {
    public boolean existsInDatabase(DBRef dbRef);
}
