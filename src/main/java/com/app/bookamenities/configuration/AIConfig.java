package com.app.bookamenities.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class AIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAIApiKey;

//    @Value("${spring.ai.anthropic.api-key}")
//    private String anthropicApiKey;

    private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();

    public OpenAiChatModel createOpenAIChatModel(String model) {
        log.info("Creating OpenAI Chat Model with model: {}", model);
        return OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder().apiKey(openAIApiKey).build())
                .defaultOptions(OpenAiChatOptions.builder().model(model != null && !model.isEmpty() ? model : "gpt-4o-mini").build())
                .build();
    }
//
//    public AnthropicChatModel createAnthropicChatModel(String model) {
//        log.info("Creating Anthropic Chat Model with model: {}", model);
//        return AnthropicChatModel.builder()
//                .anthropicApi(AnthropicApi.builder().apiKey(anthropicApiKey).build())
//                .defaultOptions(AnthropicChatOptions.builder()
//                        .model(model != null && !model.isEmpty() ? model : "claude-2")
//                        .maxTokens(500).build())
//                .build();
//    }

//    @Bean
//    public ChatClient.Builder chatClientBuilder(OpenAiChatModel chatModel) {
//        return ChatClient.builder(chatModel);
//    }
//
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public ChatMemory chatMemory(InMemoryChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    @Bean
    public InMemoryChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }


//    public ChatClient getChatClient(String provider, String model) {
//        String key = provider + ":" + model;
//        return clientCache.computeIfAbsent(key, k -> {
//            ChatModel chatModel;
//            if("openai".equalsIgnoreCase(provider)){
//                chatModel = createOpenAIChatModel(model);
//            }else if ("anthropic".equalsIgnoreCase(provider)) {
//                chatModel = createAnthropicChatModel(model);
//            } else {
//                throw new IllegalArgumentException("Unsupported provider: " + provider);
//            }
//            return ChatClient.create(chatModel);
//        });
//    }
}

