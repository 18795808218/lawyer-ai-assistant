package com.quince.lawyeraiassistant.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class LoggingAdvisorV2 implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisorV2.class);

    private static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    private static final String UNKNOWN_CONVERSATION_ID = "unknown";

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain) {

        String requestId = UUID.randomUUID().toString();

        String conversationId = extractConversationId(request);

        long startTime = System.nanoTime();

        String status = "FAILED";

        log.info(
                "AI request started, requestId={}, conversationId={}, advisor={}",
                requestId,
                conversationId,
                getName());

        try {
            ChatClientResponse response = chain.nextCall(request);

            status = "SUCCESS";

            Usage usage = extractUsage(response);

            log.info(
                    "AI request succeeded, requestId={}, conversationId={}, " +
                            "promptTokens={}, completionTokens={}, totalTokens={}",
                    requestId,
                    conversationId,
                    getPromptTokens(usage),
                    getCompletionTokens(usage),
                    getTotalTokens(usage));

            return response;
        } catch (RuntimeException exception) {
            log.error(
                    "AI request failed, requestId={}, conversationId={}, " +
                            "exceptionType={}, message={}",
                    requestId,
                    conversationId,
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception);

            throw exception;
        } finally {
            long durationNanos = System.nanoTime() - startTime;

            long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);

            log.info(
                    "AI request finished, requestId={}, conversationId={}, " +
                            "status={}, durationMs={}",
                    requestId,
                    conversationId,
                    status,
                    durationMillis);
        }
    }

    private String extractConversationId(
            ChatClientRequest request) {

        Object value = request.context()
                .get(ChatMemory.CONVERSATION_ID);

        if (value == null) {
            return UNKNOWN_CONVERSATION_ID;
        }

        return value.toString();
    }

    private Usage extractUsage(
            ChatClientResponse response) {

        if (response == null
                || response.chatResponse() == null
                || response.chatResponse().getMetadata() == null) {

            return null;
        }

        return response.chatResponse()
                .getMetadata()
                .getUsage();
    }

    private Integer getPromptTokens(Usage usage) {
        return usage == null
                ? null
                : usage.getPromptTokens();
    }

    private Integer getCompletionTokens(Usage usage) {
        return usage == null
                ? null
                : usage.getCompletionTokens();
    }

    private Integer getTotalTokens(Usage usage) {
        return usage == null
                ? null
                : usage.getTotalTokens();
    }

    @Override
    public String getName() {
        return LoggingAdvisorV2.class.getSimpleName();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}