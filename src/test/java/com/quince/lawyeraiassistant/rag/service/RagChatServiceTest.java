package com.quince.lawyeraiassistant.rag.service;

import com.quince.lawyeraiassistant.prompt.builder.LegalPromptBuilder;
import com.quince.lawyeraiassistant.prompt.model.PromptContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagChatServiceTest {

    private ChatClient ragChatClient;

    private LegalPromptBuilder legalPromptBuilder;

    private ChatClient.ChatClientRequestSpec requestSpec;

    private ChatClient.CallResponseSpec responseSpec;

    private Prompt prompt;

    private RagChatService ragChatService;

    @BeforeEach
    void setUp() {
        ragChatClient = mock(ChatClient.class);

        legalPromptBuilder = mock(LegalPromptBuilder.class);

        requestSpec = mock(
                ChatClient.ChatClientRequestSpec.class);

        responseSpec = mock(
                ChatClient.CallResponseSpec.class);

        prompt = mock(Prompt.class);

        ragChatService = new RagChatService(
                ragChatClient,
                legalPromptBuilder);
    }

    @Test
    void shouldBuildPromptAndReturnChatContent() {
        prepareSuccessfulChat("模型回答");

        String result = ragChatService.chat(
                "劳动合同解除需要赔偿吗？");

        assertEquals("模型回答", result);

        verify(
                legalPromptBuilder,
                times(1)).build(any(PromptContext.class));

        verify(
                ragChatClient,
                times(1)).prompt(prompt);

        verify(
                requestSpec,
                times(1)).call();

        verify(
                responseSpec,
                times(1)).content();
    }

    @Test
    void shouldCreatePromptContextFromQuestion() {
        prepareSuccessfulChat("回答");

        ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(
                PromptContext.class);

        ragChatService.chat(
                "经济补偿金如何计算？");

        verify(
                legalPromptBuilder).build(contextCaptor.capture());

        PromptContext context = contextCaptor.getValue();

        assertEquals(
                "经济补偿金如何计算？",
                context.getQuestion());

        assertEquals(
                "劳动法",
                context.safeVariables()
                        .get("legalDomain"));

        assertNull(context.getConversationId());
        assertFalse(context.hasConversationId());
    }

    @Test
    void shouldAddConversationIdToPromptContext() {
        prepareSuccessfulChat("回答");

        ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(
                PromptContext.class);

        ragChatService.chat(
                "试用期被辞退怎么办？",
                "conversation-001");

        verify(
                legalPromptBuilder).build(contextCaptor.capture());

        PromptContext context = contextCaptor.getValue();

        assertEquals(
                "试用期被辞退怎么办？",
                context.getQuestion());

        assertEquals(
                "conversation-001",
                context.getConversationId());

        assertTrue(context.hasConversationId());
    }

    @Test
    void shouldTrimConversationId() {
        prepareSuccessfulChat("回答");

        ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(
                PromptContext.class);

        ragChatService.chat(
                "劳动仲裁如何申请？",
                "  conversation-002  ");

        verify(
                legalPromptBuilder).build(contextCaptor.capture());

        assertEquals(
                "conversation-002",
                contextCaptor
                        .getValue()
                        .getConversationId());
    }

    @Test
    void shouldConvertBlankConversationIdToNull() {
        prepareSuccessfulChat("回答");

        ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(
                PromptContext.class);

        ragChatService.chat(
                "劳动合同问题",
                "   ");

        verify(
                legalPromptBuilder).build(contextCaptor.capture());

        PromptContext context = contextCaptor.getValue();

        assertNull(context.getConversationId());
        assertFalse(context.hasConversationId());
    }

    @Test
    void shouldThrowExceptionWhenQuestionIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ragChatService.chat(null));

        assertEquals(
                "Question must not be null",
                exception.getMessage());

        verify(
                legalPromptBuilder,
                never()).build(any());

        verify(
                ragChatClient,
                never()).prompt(any(Prompt.class));
    }

    @Test
    void shouldThrowExceptionWhenQuestionIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ragChatService.chat("   "));

        assertEquals(
                "Question must not be blank",
                exception.getMessage());

        verify(
                legalPromptBuilder,
                never()).build(any());

        verify(
                ragChatClient,
                never()).prompt(any(Prompt.class));
    }

    @Test
    void shouldPropagatePromptBuilderException() {
        IllegalStateException expectedException = new IllegalStateException(
                "Prompt build failed");

        when(
                legalPromptBuilder.build(
                        any(PromptContext.class)))
                .thenThrow(expectedException);

        IllegalStateException actualException = assertThrows(
                IllegalStateException.class,
                () -> ragChatService.chat(
                        "劳动合同问题"));

        assertEquals(
                expectedException,
                actualException);

        verify(
                ragChatClient,
                never()).prompt(any(Prompt.class));
    }

    @Test
    void shouldRejectNullConstructorDependencies() {
        NullPointerException chatClientException = assertThrows(
                NullPointerException.class,
                () -> new RagChatService(
                        null,
                        legalPromptBuilder));

        assertEquals(
                "ragChatClient must not be null",
                chatClientException.getMessage());

        NullPointerException builderException = assertThrows(
                NullPointerException.class,
                () -> new RagChatService(
                        ragChatClient,
                        null));

        assertEquals(
                "legalPromptBuilder must not be null",
                builderException.getMessage());
    }

    private void prepareSuccessfulChat(
            String responseContent) {
        when(
                legalPromptBuilder.build(
                        any(PromptContext.class)))
                .thenReturn(prompt);

        when(
                ragChatClient.prompt(prompt)).thenReturn(requestSpec);

        when(
                requestSpec.call()).thenReturn(responseSpec);

        when(
                responseSpec.content()).thenReturn(responseContent);
    }
}