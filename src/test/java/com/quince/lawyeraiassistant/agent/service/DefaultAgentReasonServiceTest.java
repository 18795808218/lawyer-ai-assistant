package com.quince.lawyeraiassistant.agent.service;

import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.prompt.model.ReasonPromptContext;
import com.quince.lawyeraiassistant.prompt.builder.PromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.Prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultAgentReasonServiceTest {

    private ChatClient chatClient;

    private PromptBuilder promptBuilder;

    private DefaultAgentReasonService reasonService;

    private ChatClientRequestSpec requestSpec;

    private CallResponseSpec responseSpec;

    @BeforeEach
    void setUp() {

        chatClient = mock(ChatClient.class);

        promptBuilder = mock(PromptBuilder.class);

        requestSpec = mock(ChatClientRequestSpec.class);

        responseSpec = mock(CallResponseSpec.class);

        reasonService = new DefaultAgentReasonService(
                chatClient,
                promptBuilder);
    }

    @Test
    void shouldGenerateReasonResult() {

        ReasonPromptContext context = ReasonPromptContext.from(
                "分析劳动合同并生成律师意见书");

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildReason(
                        context))
                .thenReturn(
                        prompt);

        when(
                chatClient.prompt(
                        prompt))
                .thenReturn(
                        requestSpec);

        when(
                requestSpec.call()).thenReturn(
                        responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        "用户希望分析劳动合同并生成律师意见书。");

        ReasonResult result = reasonService.reason(
                context);

        assertNotNull(
                result);

        assertEquals(
                "用户希望分析劳动合同并生成律师意见书。",
                result.getReasonSummary());

        verify(
                promptBuilder).buildReason(
                        context);

        verify(
                chatClient).prompt(
                        prompt);

        verify(
                requestSpec).call();

        verify(
                responseSpec).content();
    }

    @Test
    void shouldTrimReasonSummary() {

        ReasonPromptContext context = ReasonPromptContext.from(
                "测试");

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildReason(
                        context))
                .thenReturn(
                        prompt);

        when(
                chatClient.prompt(
                        prompt))
                .thenReturn(
                        requestSpec);

        when(
                requestSpec.call()).thenReturn(
                        responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        "   用户希望测试。   ");

        ReasonResult result = reasonService.reason(
                context);

        assertEquals(
                "用户希望测试。",
                result.getReasonSummary());
    }

    @Test
    void shouldRejectNullContext() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> reasonService.reason(
                        null));

        assertEquals(
                "ReasonPromptContext must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankReasonResult() {

        ReasonPromptContext context = ReasonPromptContext.from(
                "测试");

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildReason(
                        context))
                .thenReturn(
                        prompt);

        when(
                chatClient.prompt(
                        prompt))
                .thenReturn(
                        requestSpec);

        when(
                requestSpec.call()).thenReturn(
                        responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        "   ");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reasonService.reason(
                        context));

        assertEquals(
                "Reason result must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullReasonResult() {

        ReasonPromptContext context = ReasonPromptContext.from(
                "测试");

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildReason(
                        context))
                .thenReturn(
                        prompt);

        when(
                chatClient.prompt(
                        prompt))
                .thenReturn(
                        requestSpec);

        when(
                requestSpec.call()).thenReturn(
                        responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> reasonService.reason(
                        context));

        assertEquals(
                "Reason result must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldCallPromptBuilderOnlyOnce() {

        ReasonPromptContext context = ReasonPromptContext.from(
                "测试");

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildReason(
                        any()))
                .thenReturn(
                        prompt);

        when(
                chatClient.prompt(
                        any(Prompt.class)))
                .thenReturn(
                        requestSpec);

        when(
                requestSpec.call()).thenReturn(
                        responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        "测试");

        reasonService.reason(
                context);

        verify(
                promptBuilder,
                times(1)).buildReason(
                        context);
    }
}