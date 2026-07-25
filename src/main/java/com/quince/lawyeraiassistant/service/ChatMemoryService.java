package com.quince.lawyeraiassistant.service;

import com.quince.lawyeraiassistant.dto.request.ChatMemoryRequest;
import com.quince.lawyeraiassistant.dto.response.ChatMemoryResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ChatMemoryService {

    private final ChatClient memoryChatClient;

    public ChatMemoryService(
            @Qualifier("memoryChatClient") ChatClient memoryChatClient) {

        this.memoryChatClient = memoryChatClient;
    }

    public ChatMemoryResponse chat(ChatMemoryRequest request) {

        String conversationId = request.getConversationId().strip();

        String message = request.getMessage().strip();

        String answer = memoryChatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(
                        ChatMemory.CONVERSATION_ID,
                        conversationId))
                .call()
                .content();

        return new ChatMemoryResponse(
                conversationId,
                answer);
    }
}