package com.app.bookamenities.service;

import com.app.bookamenities.dto.CustomChatRequest;
import com.app.bookamenities.dto.CustomChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final BookAmenitiesChatTool bookAmenitiesChatTool;
    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    public ChatService(ChatClient chatClient, BookAmenitiesChatTool bookAmenitiesChatTool, VectorStore vectorStore, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.bookAmenitiesChatTool = bookAmenitiesChatTool;
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;
    }

    public CustomChatResponse generateTextResponse(CustomChatRequest chatRequest) {

        log.info("Generating text response for query: {} ", chatRequest.getQuery());

        String userPrompt = """
                User Question: %s
                
                Logged in User ID: %s
                
                Booking Id: %s
                
                IMPORTANT INSTRUCTIONS:
                - Always use tools for booking-related operations.
                - For cancel booking requests, extract the booking ID from the user question and pass the Booking id to the appropriate tool.
                - Always pass the logged in user ID to the tool, if the tool required User ID.
                - Never ask the user again for booking ID if already present in the question.
                """.formatted(
                chatRequest.getQuery(),
                chatRequest.getUserId(),
                chatRequest.getBookingId()
        );
        log.info("ConversationId = {}", chatRequest.getConversationId());

        String response = chatClient.prompt()
                .user(userPrompt)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                                .conversationId(chatRequest.getConversationId())
                                .build(),
                        new QuestionAnswerAdvisor(vectorStore))
                .tools(bookAmenitiesChatTool)
                .call()
                .content();

        log.info("ChatMemory bean = {}", chatMemory.getClass());

        CustomChatResponse customChatResponse = new CustomChatResponse();
        customChatResponse.setResponse(response);
        return customChatResponse;
    }
}
