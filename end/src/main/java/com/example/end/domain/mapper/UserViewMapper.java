package com.example.end.domain.mapper;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.LoggedInUser;
import com.example.end.domain.model.User;
import com.mongodb.DBRef;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class UserViewMapper {
    public abstract LoggedInUser toLoggedInUser(User user, Set<Contact> contacts);

    @Mapping(source = "id", target = "ref", qualifiedByName = "fromId")
    public abstract Contact toContact(User user);

    @Named("fromId")
    public DBRef fromId(String id) {
        return new DBRef("users", id);
    }
}
