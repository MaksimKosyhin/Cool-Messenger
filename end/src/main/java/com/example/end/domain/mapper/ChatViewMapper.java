package com.example.end.domain.mapper;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.PersonalContact;
import com.example.end.domain.model.Chat;
import com.example.end.domain.model.ChatMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface ChatViewMapper {

    @Mapping(target = "displayName", source = "title")
    public Contact toContact(Chat chat);
    @Mapping(target = "displayName", source = "chat.title")
    public PersonalContact toPersonalContact(Chat chat, Set<ChatMember.Permission> permissions);
}
