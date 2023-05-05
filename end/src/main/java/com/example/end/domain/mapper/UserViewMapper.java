package com.example.end.domain.mapper;

import com.example.end.domain.dto.LoggedInUser;
import com.example.end.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserViewMapper {
    public LoggedInUser toLoggedInUser(User user);
}
