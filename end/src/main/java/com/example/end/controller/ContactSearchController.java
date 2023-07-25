package com.example.end.controller;

import com.example.end.domain.dto.ChatMembersQuery;
import com.example.end.domain.dto.ContactQuery;
import com.example.end.service.ContactSearchService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/v1/contacts")
@RequiredArgsConstructor
public class ContactSearchController {
    private final ContactSearchService contactSearchService;

    @GetMapping("chat/{chatId}")
    public ResponseEntity<?> getChatMembers(@PathVariable ObjectId chatId,
                                            @RequestParam int page,
                                            @RequestParam int size) {

        var result = contactSearchService.getChatMembers(chatId, PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("chat/{chatId}/search")
    public ResponseEntity<?> searchChatMembers(@PathVariable ObjectId chatId,
                                               @RequestBody ChatMembersQuery contactQuery,
                                               @RequestParam int page,
                                               @RequestParam int size) {

        var result =
                contactSearchService.searchChatMembers(chatId, contactQuery, PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("user/{username}")
    public ResponseEntity<?> getPersonalContacts(@PathVariable String username) {
        var result = contactSearchService.getPersonalContacts(username);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping
    public ResponseEntity<?> searchContacts(@RequestBody ContactQuery contactQuery,
                                            @RequestParam int page,
                                            @RequestParam int size) {

        var result = contactSearchService.searchContacts(contactQuery, PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
