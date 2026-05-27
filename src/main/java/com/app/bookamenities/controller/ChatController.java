package com.app.bookamenities.controller;

import com.app.bookamenities.dto.CustomChatRequest;
import com.app.bookamenities.dto.CustomChatResponse;
import com.app.bookamenities.service.ChatService;
import com.app.bookamenities.service.DataLoadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-amenities/chat-client")
public class ChatController {

    private final ChatService chatService;

    private final DataLoadService dataLoadService;

    public ChatController(ChatService chatService, DataLoadService dataLoadService) {
        this.chatService = chatService;
        this.dataLoadService = dataLoadService;
    }

    @PostMapping
    public CustomChatResponse askText(@RequestBody CustomChatRequest chatRequest) {
        return chatService.generateTextResponse(chatRequest);
    }

    @PostMapping("/reload")
    public ResponseEntity<String> reloadRagData() {
        dataLoadService.loadData();
        return ResponseEntity.ok("RAG data reloaded successfully");
    }
}