package com.example.end.repository;

import com.example.end.domain.dto.Contact;
import com.example.end.domain.model.EntityReference;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ContactsDao {
    public boolean chatExists(String id);
    public Set<Contact> getContacts(String username, Pageable page);

    public String joinChat(String username, EntityReference ref);

    public String leaveChat(String username, String id);
}
