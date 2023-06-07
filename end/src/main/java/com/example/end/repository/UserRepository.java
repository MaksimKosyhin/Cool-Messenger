package com.example.end.repository;

import com.example.end.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    public boolean existsByEmail(String email);

    public boolean existsByUsername(String username);
    public Optional<User> findByUsername(String username);

    @Query("SELECT u.id FROM user u WHERE u.username :username")
    public String findIdByUsername(@Param("username") String username);

}