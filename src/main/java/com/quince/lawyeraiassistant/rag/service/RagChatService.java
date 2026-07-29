package com.quince.lawyeraiassistant.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RagChatService {

    private final ChatClient ragChatClient;
    private final Resource lawyerSystemPromptResource;

    public RagChatService(
            @Qualifier("ragChatClient") ChatClient ragChatClient,
            @Value("classpath:prompts/lawyer-system.st") Resource lawyerSystemPromptResource) {

        this.ragChatClient = ragChatClient;
        this.lawyerSystemPromptResource = lawyerSystemPromptResource;
    }

    public String chat(String question) {

        PromptTemplate systemPromptTemplate = new PromptTemplate(lawyerSystemPromptResource);

        String systemPrompt = systemPromptTemplate.render(
                Map.of(
                        "legalDomain",
                        "劳动法"));

        return ragChatClient
                .prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();
    }
}