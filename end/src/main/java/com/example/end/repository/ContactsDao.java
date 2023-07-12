package com.example.end.repository;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.dto.PersonalContact;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContactsDao {
    public List<PersonalContact> getContacts(String username);
    //todo: add contact upon channel creation
    public boolean contactExists(ObjectId chatId);
    public boolean contactExists(String identifier);
    public boolean addContact(String username, ObjectId chatId);
    public boolean removeContact(String username, ObjectId chatId);
    public List<Contact> getChatMembers(ObjectId chatId, Pageable pageable);
}
