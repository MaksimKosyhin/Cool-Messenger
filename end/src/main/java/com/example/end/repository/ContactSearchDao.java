package com.example.end.repository;

import com.example.end.domain.dto.ChatMembersQuery;
import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.ContactQuery;
import com.example.end.domain.dto.PersonalContact;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContactSearchDao {
    public List<PersonalContact> getPersonalContacts(String username);
    public List<Contact> getChatMembers(ObjectId chatId, Pageable pageable);
    public List<Contact> searchChatMembers(ObjectId chatId, ChatMembersQuery query, Pageable pageable);
    public List<Contact> searchContacts(ContactQuery query, Pageable pageable);
}
