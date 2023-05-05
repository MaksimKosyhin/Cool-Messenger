package com.example.end.domain.mapper;

import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class UserEditMapper {
    public abstract User create(CreateUserRequest request);

    @Named("defaultFolder")
    public Map<String, List<String>> defaultFolder(){
        return Map.of("all", Collections.emptyList());
    }
}
