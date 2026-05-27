package com.app.bookamenities.service;

import com.app.bookamenities.dto.CustomChatRequest;
import com.app.bookamenities.dto.CustomChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final BookAmenitiesChatTool bookAmenitiesChatTool;
    private final VectorStore vectorStore;

    public ChatService(ChatClient chatClient, BookAmenitiesChatTool bookAmenitiesChatTool, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.bookAmenitiesChatTool = bookAmenitiesChatTool;
        this.vectorStore = vectorStore;
    }

    public CustomChatResponse generateTextResponse(CustomChatRequest chatRequest) {

        log.info("Generating text response for query: {} ", chatRequest.getQuery());

        String userPrompt = """
            User Question: %s
            
            Logged in User ID: %s
            
            Use the provided tools whenever booking-related real-time data is needed.
            """.formatted(
                chatRequest.getQuery(),
                chatRequest.getUserId()
        );

        String response = chatClient.prompt()
                .user(userPrompt)
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .tools(bookAmenitiesChatTool)
                .call()
                .content();

        CustomChatResponse customChatResponse = new CustomChatResponse();
        customChatResponse.setResponse(response);
        return customChatResponse;
    }
}
