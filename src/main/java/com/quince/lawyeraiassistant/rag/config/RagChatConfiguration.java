package com.quince.lawyeraiassistant.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.quince.lawyeraiassistant.advisor.LoggingAdvisorV2;

@Configuration
public class RagChatConfiguration {

    @Bean("ragChatClient")
    public ChatClient ragChatClient(
            ChatClient.Builder builder,
            RetrievalAugmentationAdvisor retrievalAugmentationAdvisor,
            LoggingAdvisorV2 loggingAdvisorV2) {

        return builder
                .defaultAdvisors(loggingAdvisorV2, retrievalAugmentationAdvisor)
                .build();
    }
}