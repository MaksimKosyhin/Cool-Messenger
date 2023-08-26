package com.example.end.service;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.CreateChatRequest;
import com.example.end.domain.dto.PersonalContact;
import com.example.end.domain.dto.UpdateChatRequest;
import com.example.end.domain.mapper.ChatEditMapper;
import com.example.end.domain.mapper.ChatViewMapper;
import com.example.end.domain.model.Chat;
import com.example.end.domain.model.ChatMember;
import com.example.end.exception.ApiException;
import com.example.end.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService{
    private final ChatRepository chatRepository;
    private final FileService fileService;
    private final ChatViewMapper chatViewMapper;
    private final ChatEditMapper chatEditMapper;
    @Value("spring.minio.buckets.app")
    private String appBucket;

    @Override
    public PersonalContact createChat(CreateChatRequest request) {
        var chat = chatEditMapper.create(request);
        chat = chatRepository.save(chat);
        return chatViewMapper.toPersonalContact(chat, new HashSet<>());
    }

    @Override
    public Set<ChatMember.Permission> getDefaultPermissions(ObjectId chatId) {
        var chat = getChatOrThrow(chatId);
        return chat.getDefaultPermissions();
    }

    @Override
    public Contact updateChatInfo(ObjectId chatId, UpdateChatRequest request) {
        Chat chat = getChatOrThrow(chatId);

        chatEditMapper.update(request, chat);
        chat = chatRepository.save(chat);
        return chatViewMapper.toContact(chat);
    }

    @Override
    public String updateChatImage(ObjectId chatId, MultipartFile file) {
        if(!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    String.format("not supported image extension: s%", file.getContentType()));
        }

        var chat = getChatOrThrow(chatId);

        if(chat.getImagePath() != null) {
            fileService.deleteFile(appBucket, chat.getImagePath());
            if (file.isEmpty()) {
                return null;
            }
        }

        var imageId = UUID.randomUUID().toString();
        var imagePath = Path.of("contact-images", chat.getId().toHexString(), imageId).toString();

        fileService.uploadFile(appBucket, imagePath, file);
        chat.setImagePath(imagePath);
        chatRepository.save(chat);

        return imagePath;
    }

    @Override
    public boolean existsByIdentifier(String identifier) {
        return chatRepository.existsByIdentifier(identifier);
    }

    private Chat getChatOrThrow(ObjectId chatId) {
        var chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        String.format("chat with id: %s doesn't exists")));
        return chat;
    }

    @Override
    public void deleteChat(ObjectId chatId) {
        chatRepository.deleteById(chatId);
    }
}
