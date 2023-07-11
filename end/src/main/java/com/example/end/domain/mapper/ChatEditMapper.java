package com.example.end.domain.mapper;

import com.example.end.domain.dto.CreateChatRequest;
import com.example.end.domain.dto.UpdateChatRequest;
import com.example.end.domain.model.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChatEditMapper {
    public Chat create(CreateChatRequest request);
    public void update(UpdateChatRequest request, @MappingTarget Chat chat);
}
