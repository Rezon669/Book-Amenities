package com.app.bookamenities.controller;

import com.app.bookamenities.dto.CustomChatRequest;
import com.app.bookamenities.dto.CustomChatResponse;
import com.app.bookamenities.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-amenities/chat-client")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public CustomChatResponse askText(@RequestBody CustomChatRequest chatRequest) {
        return chatService.generateTextResponse(chatRequest);
    }
}