package com.quince.lawyeraiassistant.rag.service;

import com.quince.lawyeraiassistant.prompt.builder.LegalPromptBuilder;
import com.quince.lawyeraiassistant.prompt.model.PromptContext;
import com.quince.lawyeraiassistant.query.model.QueryContext;
import com.quince.lawyeraiassistant.retrieval.model.RetrieverContext;
import com.quince.lawyeraiassistant.retrieval.orchestration.RetrievalOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

        private RetrievalOrchestrator retrievalOrchestrator;

        private LegalPromptBuilder legalPromptBuilder;

        private ChatClient.ChatClientRequestSpec requestSpec;

        private ChatClient.CallResponseSpec responseSpec;

        private Prompt prompt;

        private RagChatService ragChatService;

        @BeforeEach
        void setUp() {
                ragChatClient = mock(ChatClient.class);

                retrievalOrchestrator = mock(RetrievalOrchestrator.class);

                legalPromptBuilder = mock(LegalPromptBuilder.class);

                requestSpec = mock(
                                ChatClient.ChatClientRequestSpec.class);

                responseSpec = mock(
                                ChatClient.CallResponseSpec.class);

                prompt = mock(Prompt.class);

                ragChatService = new RagChatService(
                                ragChatClient,
                                retrievalOrchestrator,
                                legalPromptBuilder);
        }

        @Test
        void shouldRetrieveBuildPromptAndReturnChatContent() {
                List<Document> documents = List.of(
                                new Document(
                                                "劳动合同法第四十六条"),
                                new Document(
                                                "劳动合同法第四十七条"));

                prepareRetrieval(
                                "劳动合同解除需要赔偿吗？",
                                null,
                                "劳动合同解除需要赔偿吗？",
                                documents);

                prepareSuccessfulChat(
                                "模型回答");

                String result = ragChatService.chat(
                                "劳动合同解除需要赔偿吗？");

                assertEquals(
                                "模型回答",
                                result);

                verify(
                                retrievalOrchestrator,
                                times(1)).retrieve(
                                                "劳动合同解除需要赔偿吗？",
                                                null);

                verify(
                                legalPromptBuilder,
                                times(1)).build(
                                                any(PromptContext.class));

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
        void shouldUseOriginalQuestionAndRetrievedDocumentsInPromptContext() {
                List<Document> documents = List.of(
                                new Document(
                                                "劳动合同法第三十九条"),
                                new Document(
                                                "劳动合同法第八十七条"));

                prepareRetrieval(
                                "老板突然不要我了，合法吗？",
                                "conversation-001",
                                "违法解除劳动合同是否合法",
                                documents);

                prepareSuccessfulChat("回答");

                ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(
                                PromptContext.class);

                ragChatService.chat(
                                "老板突然不要我了，合法吗？",
                                "conversation-001");

                verify(
                                legalPromptBuilder).build(
                                                contextCaptor.capture());

                PromptContext context = contextCaptor.getValue();

                /*
                 * 最终 Prompt 使用原始问题，
                 * 而不是 rewriteQuery。
                 */
                assertEquals(
                                "老板突然不要我了，合法吗？",
                                context.getQuestion());

                assertEquals(
                                "conversation-001",
                                context.getConversationId());

                assertEquals(
                                "劳动法",
                                context.safeVariables()
                                                .get("legalDomain"));

                assertEquals(
                                documents,
                                context.safeKnowledge());

                assertTrue(
                                context.hasKnowledge());
        }

        @Test
        void shouldNotUseRewriteQueryAsUserQuestion() {
                prepareRetrieval(
                                "老板把我开了合法吗？",
                                null,
                                "违法解除劳动合同是否合法",
                                List.of(
                                                new Document(
                                                                "相关法律知识")));

                prepareSuccessfulChat("回答");

                ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(
                                PromptContext.class);

                ragChatService.chat(
                                "老板把我开了合法吗？");

                verify(
                                legalPromptBuilder).build(
                                                contextCaptor.capture());

                assertEquals(
                                "老板把我开了合法吗？",
                                contextCaptor
                                                .getValue()
                                                .getQuestion());
        }

        @Test
        void shouldPassNormalizedConversationIdToRetrievalOrchestrator() {
                prepareRetrieval(
                                "劳动仲裁如何申请？",
                                "conversation-002",
                                "劳动仲裁申请流程",
                                List.of());

                prepareSuccessfulChat("回答");

                ragChatService.chat(
                                "劳动仲裁如何申请？",
                                "  conversation-002  ");

                verify(
                                retrievalOrchestrator).retrieve(
                                                "劳动仲裁如何申请？",
                                                "conversation-002");
        }

        @Test
        void shouldConvertBlankConversationIdToNull() {
                prepareRetrieval(
                                "劳动合同问题",
                                null,
                                "劳动合同问题",
                                List.of());

                prepareSuccessfulChat("回答");

                ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(
                                PromptContext.class);

                ragChatService.chat(
                                "劳动合同问题",
                                "   ");

                verify(
                                retrievalOrchestrator).retrieve(
                                                "劳动合同问题",
                                                null);

                verify(
                                legalPromptBuilder).build(
                                                contextCaptor.capture());

                assertNull(
                                contextCaptor
                                                .getValue()
                                                .getConversationId());

                assertFalse(
                                contextCaptor
                                                .getValue()
                                                .hasConversationId());
        }

        @Test
        void shouldAllowEmptyRetrievalResult() {
                prepareRetrieval(
                                "知识库中不存在的问题",
                                null,
                                "知识库中不存在的问题",
                                List.of());

                prepareSuccessfulChat(
                                "资料不足");

                ArgumentCaptor<PromptContext> contextCaptor = ArgumentCaptor.forClass(
                                PromptContext.class);

                String result = ragChatService.chat(
                                "知识库中不存在的问题");

                verify(
                                legalPromptBuilder).build(
                                                contextCaptor.capture());

                assertFalse(
                                contextCaptor
                                                .getValue()
                                                .hasKnowledge());

                assertTrue(
                                contextCaptor
                                                .getValue()
                                                .safeKnowledge()
                                                .isEmpty());

                assertEquals(
                                "资料不足",
                                result);
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
                                retrievalOrchestrator,
                                never()).retrieve(
                                                any(),
                                                any());

                verify(
                                legalPromptBuilder,
                                never()).build(any());

                verify(
                                ragChatClient,
                                never()).prompt(
                                                any(Prompt.class));
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
                                retrievalOrchestrator,
                                never()).retrieve(
                                                any(),
                                                any());

                verify(
                                legalPromptBuilder,
                                never()).build(any());

                verify(
                                ragChatClient,
                                never()).prompt(
                                                any(Prompt.class));
        }

        @Test
        void shouldPropagateRetrievalException() {
                IllegalStateException expectedException = new IllegalStateException(
                                "Vector search failed");

                when(
                                retrievalOrchestrator.retrieve(
                                                "劳动合同问题",
                                                null))
                                .thenThrow(expectedException);

                IllegalStateException actualException = assertThrows(
                                IllegalStateException.class,
                                () -> ragChatService.chat(
                                                "劳动合同问题"));

                assertSame(
                                expectedException,
                                actualException);

                verify(
                                legalPromptBuilder,
                                never()).build(any());

                verify(
                                ragChatClient,
                                never()).prompt(
                                                any(Prompt.class));
        }

        @Test
        void shouldPropagatePromptBuilderException() {
                prepareRetrieval(
                                "劳动合同问题",
                                null,
                                "劳动合同问题",
                                List.of(
                                                new Document("知识")));

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

                assertSame(
                                expectedException,
                                actualException);

                verify(
                                ragChatClient,
                                never()).prompt(
                                                any(Prompt.class));
        }

        @Test
        void shouldRejectNullConstructorDependencies() {
                NullPointerException chatClientException = assertThrows(
                                NullPointerException.class,
                                () -> new RagChatService(
                                                null,
                                                retrievalOrchestrator,
                                                legalPromptBuilder));

                assertEquals(
                                "ragChatClient must not be null",
                                chatClientException.getMessage());

                NullPointerException orchestratorException = assertThrows(
                                NullPointerException.class,
                                () -> new RagChatService(
                                                ragChatClient,
                                                null,
                                                legalPromptBuilder));

                assertEquals(
                                "retrievalOrchestrator must not be null",
                                orchestratorException.getMessage());

                NullPointerException builderException = assertThrows(
                                NullPointerException.class,
                                () -> new RagChatService(
                                                ragChatClient,
                                                retrievalOrchestrator,
                                                null));

                assertEquals(
                                "legalPromptBuilder must not be null",
                                builderException.getMessage());
        }

        private void prepareRetrieval(
                        String question,
                        String conversationId,
                        String rewriteQuery,
                        List<Document> documents) {

                QueryContext queryContext = QueryContext.builder()
                                .question(question)
                                .conversationId(
                                                conversationId)
                                .rewriteQuery(
                                                rewriteQuery)
                                .build();

                RetrieverContext retrieverContext = RetrieverContext.builder()
                                .queryContext(
                                                queryContext)
                                .documents(
                                                documents)
                                .build();

                when(
                                retrievalOrchestrator.retrieve(
                                                question,
                                                conversationId))
                                .thenReturn(
                                                retrieverContext);
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