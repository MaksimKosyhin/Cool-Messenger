package com.example.end.domain.mapper;

import com.example.end.domain.dto.LoggedInUser;
import com.example.end.domain.dto.UserView;
import com.example.end.domain.model.User;
import com.mongodb.DBRef;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class UserViewMapper {
    public abstract LoggedInUser toLoggedInUser(User user);

    @Mapping(source = "id", target = "ref", qualifiedByName = "fromId")
    public abstract UserView toUserView(User user);

    @Named("fromId")
    public DBRef fromId(String id) {
        return new DBRef("users", id);
    }
}
