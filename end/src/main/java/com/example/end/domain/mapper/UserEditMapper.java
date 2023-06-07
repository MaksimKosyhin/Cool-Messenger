package com.example.end.domain.mapper;

import com.example.end.domain.dto.CreateUserRequest;
import com.example.end.domain.dto.UpdateUserRequest;
import com.example.end.domain.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public abstract class UserEditMapper {
    @Mapping( target = "folders", expression = "java(defaultFolder())")
    public abstract User create(CreateUserRequest request);

    public Map<String, List<String>> defaultFolder(){
        return Map.of("all", Collections.emptyList());
    }

    @BeanMapping(nullValueCheckStrategy = ALWAYS, nullValuePropertyMappingStrategy = IGNORE)
    public abstract void update(UpdateUserRequest request, @MappingTarget User user);
}
