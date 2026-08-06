package com.quince.lawyeraiassistant.agent.service;

import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.prompt.model.ReasonPromptContext;
import com.quince.lawyeraiassistant.prompt.builder.PromptBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DefaultAgentReasonService
        implements AgentReasonService {

    private final ChatClient chatClient;

    private final PromptBuilder promptBuilder;

    public DefaultAgentReasonService(
            @Qualifier("agentReasonChatClient") ChatClient chatClient,
            PromptBuilder promptBuilder) {

        this.chatClient = Objects.requireNonNull(
                chatClient);

        this.promptBuilder = Objects.requireNonNull(
                promptBuilder);
    }

    @Override
    public ReasonResult reason(
            ReasonPromptContext context) {

        Objects.requireNonNull(
                context,
                "ReasonPromptContext must not be null");

        Prompt prompt = promptBuilder.buildReason(
                context);

        String content = chatClient
                .prompt(prompt)
                .call()
                .content();

        if (content == null
                || content.isBlank()) {

            throw new IllegalStateException(
                    "Reason result must not be blank");
        }

        return ReasonResult.from(
                content.trim());
    }

}