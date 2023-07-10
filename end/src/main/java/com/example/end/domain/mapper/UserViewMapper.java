package com.example.end.domain.mapper;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.LoggedInUser;
import com.example.end.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class UserViewMapper {
    public abstract LoggedInUser toLoggedInUser(User user);

    @Mapping(target = "identifier", source = "username")
    public abstract Contact toContact(User user);
}
