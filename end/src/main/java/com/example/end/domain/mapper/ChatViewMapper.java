package com.example.end.domain.mapper;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.model.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatViewMapper {

    @Mapping(target = "displayName", source = "title")
    public Contact toContact(Chat chat);
}
