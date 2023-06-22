package com.example.end.service;

import com.example.end.domain.dto.Contact;
import org.springframework.data.domain.Page;

public interface ChatService {
    public Page<Contact> getChatMembers(String chatId);

}
