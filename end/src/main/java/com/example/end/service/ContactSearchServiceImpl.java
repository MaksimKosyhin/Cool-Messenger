package com.example.end.service;

import com.example.end.domain.dto.ChatMembersQuery;
import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.ContactQuery;
import com.example.end.domain.dto.PersonalContact;
import com.example.end.exception.ApiException;
import com.example.end.repository.ContactSearchDao;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ContactSearchServiceImpl implements ContactSearchService{

    private final ContactSearchDao contactSearchDao;

    @Override
    public List<PersonalContact> getPersonalContacts(String username) {
        return contactSearchDao.getPersonalContacts(username);
    }

    @Override
    public List<Contact> getChatMembers(ObjectId chatId, Pageable pageable) {
        throwIfPageableInvalid(pageable);
        return contactSearchDao.getChatMembers(chatId, pageable);
    }

    @Override
    public List<Contact> searchChatMembers(ObjectId chatId, ChatMembersQuery query, Pageable pageable) {
        throwIfPageableInvalid(pageable);
        return contactSearchDao.searchChatMembers(chatId, query, pageable);
    }

    @Override
    public List<Contact> searchContacts(ContactQuery query, Pageable pageable) {
        throwIfPageableInvalid(pageable);

        if(query.collectionName().equals("users") || query.collectionName().equals("chats")) {
            return contactSearchDao.searchContacts(query, pageable);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid request");
        }
    }

    private void throwIfPageableInvalid(Pageable pageable) {
        if (pageable.getOffset() < 0 || pageable.getPageSize() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid page");
        }
    }
}
