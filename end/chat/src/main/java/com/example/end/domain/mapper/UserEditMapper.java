package com.example.end.domain.mapper;

import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.domain.dto.UpdateUserRequest;
import com.example.end.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserEditMapper {
    public User create(CreateUserRequest request);

    public void update(UpdateUserRequest request, @MappingTarget User user);
}
