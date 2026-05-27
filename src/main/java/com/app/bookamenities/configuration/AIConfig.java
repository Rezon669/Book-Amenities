package com.app.bookamenities.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AIConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        log.info("Creating chat model");
        return builder.build();
    }

}

