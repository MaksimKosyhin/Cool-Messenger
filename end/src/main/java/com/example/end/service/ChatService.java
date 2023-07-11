package com.example.end.service;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.CreateChatRequest;
import com.example.end.domain.dto.UpdateChatRequest;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface ChatService {
    public Contact createChat(CreateChatRequest request);

    public boolean chatExists(String identifier);
    public Contact getChat(ObjectId chatId);
    public Contact updateChatInfo(ObjectId chatId, UpdateChatRequest request);
    public Path updateChatImage(ObjectId chatId, MultipartFile file);
    public void deleteChat(ObjectId chatId);

}
