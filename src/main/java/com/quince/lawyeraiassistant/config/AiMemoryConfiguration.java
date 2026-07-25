package com.quince.lawyeraiassistant.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//import com.quince.lawyeraiassistant.advisor.LoggingAdvisor;
import com.quince.lawyeraiassistant.advisor.LoggingAdvisorV2;
import com.quince.lawyeraiassistant.advisor.SensitiveWordAdvisor;

@Configuration
public class AiMemoryConfiguration {

    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    public ChatMemory chatMemory(
            ChatMemoryRepository chatMemoryRepository) {

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient memoryChatClient(
            ChatClient.Builder builder,
            LoggingAdvisorV2 loggingAdvisorV2,
            SensitiveWordAdvisor sensitiveWordAdvisor,
            ChatMemory chatMemory) {

        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .build();

        return builder
                .defaultAdvisors(
                        loggingAdvisorV2,
                        sensitiveWordAdvisor,
                        memoryAdvisor)
                .build();
    }
}