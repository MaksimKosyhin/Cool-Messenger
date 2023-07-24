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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService{
    private final ChatRepository chatRepository;
    private final FileService fileService;
    private final ChatViewMapper chatViewMapper;
    private final ChatEditMapper chatEditMapper;

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
    public Path updateChatImage(ObjectId chatId, MultipartFile file) {
        var chat = getChatOrThrow(chatId);

        if(file.isEmpty() && chat.getImageUrl() != null) {
            fileService.delete(Paths.get(chat.getImageUrl()));
            return null;
        }

        var imagePath = Path.of(chat.getId().toString());

        Path fullPath;
        if(chat.getImageUrl() == null) {
            fullPath = fileService.saveProfileImage(file, imagePath);
        } else {
            fullPath = fileService.replaceProfileImage(file, imagePath);
        }

        chat.setImageUrl(fullPath.toString());
        chatRepository.save(chat);

        return fullPath;
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
