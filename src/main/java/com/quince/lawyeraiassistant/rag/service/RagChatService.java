package com.quince.lawyeraiassistant.rag.service;

import com.quince.lawyeraiassistant.prompt.builder.LegalPromptBuilder;
import com.quince.lawyeraiassistant.prompt.model.PromptContext;
import com.quince.lawyeraiassistant.retrieval.model.RetrieverContext;
import com.quince.lawyeraiassistant.retrieval.orchestration.RetrievalOrchestrator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 法律 RAG 问答服务。
 *
 * <p>
 * 正式调用链：
 * </p>
 *
 * <pre>
 * question
 *      ↓
 * RetrievalOrchestrator
 *      ↓
 * QueryPipeline
 *      ↓
 * RetrieverPipeline
 *      ↓
 * List&lt;Document&gt;
 *      ↓
 * PromptContext.knowledge
 *      ↓
 * LegalPromptBuilder
 *      ↓
 * ragChatClient
 *      ↓
 * LLM
 * </pre>
 *
 * <p>
 * ragChatClient 不再包含 RetrievalAugmentationAdvisor，
 * 避免同一次请求重复检索和重复注入知识。
 * </p>
 */
@Service
public class RagChatService {

        private static final String DEFAULT_LEGAL_DOMAIN = "劳动法";

        private final ChatClient ragChatClient;

        private final RetrievalOrchestrator retrievalOrchestrator;

        private final LegalPromptBuilder legalPromptBuilder;

        public RagChatService(
                        @Qualifier("ragChatClient") ChatClient ragChatClient,
                        RetrievalOrchestrator retrievalOrchestrator,
                        LegalPromptBuilder legalPromptBuilder) {

                this.ragChatClient = Objects.requireNonNull(
                                ragChatClient,
                                "ragChatClient must not be null");

                this.retrievalOrchestrator = Objects.requireNonNull(
                                retrievalOrchestrator,
                                "retrievalOrchestrator must not be null");

                this.legalPromptBuilder = Objects.requireNonNull(
                                legalPromptBuilder,
                                "legalPromptBuilder must not be null");
        }

        /**
         * 执行无会话编号的法律问答。
         */
        public String chat(String question) {
                return chat(question, null);
        }

        /**
         * 执行完整法律 RAG 问答。
         *
         * @param question       用户原始问题
         * @param conversationId 可选会话编号
         * @return 模型回答
         */
        public String chat(
                        String question,
                        String conversationId) {

                validateQuestion(question);

                /*
                 * QueryPipeline + RetrieverPipeline。
                 *
                 * Retriever 使用 effectiveQuery，
                 * QueryContext 中仍然保留原始 question。
                 */
                RetrieverContext retrievalContext = retrievalOrchestrator.retrieve(
                                question,
                                normalizeConversationId(
                                                conversationId));

                /*
                 * 将检索得到的 Document 注入 PromptContext。
                 */
                PromptContext promptContext = createPromptContext(
                                retrievalContext);

                Prompt prompt = legalPromptBuilder.build(
                                promptContext);

                /*
                 * ragChatClient 已不再包含
                 * RetrievalAugmentationAdvisor。
                 */
                return ragChatClient
                                .prompt(prompt)
                                .call()
                                .content();
        }

        /**
         * 使用 Retriever Pipeline 的结果创建 PromptContext。
         */
        private PromptContext createPromptContext(
                        RetrieverContext retrievalContext) {

                Objects.requireNonNull(
                                retrievalContext,
                                "RetrieverContext must not be null");

                return PromptContext.builder()
                                /*
                                 * 最终回答必须使用用户原始问题，
                                 * 不能使用 effectiveQuery。
                                 */
                                .question(
                                                retrievalContext
                                                                .getQueryContext()
                                                                .getQuestion())
                                .conversationId(
                                                retrievalContext
                                                                .getQueryContext()
                                                                .getConversationId())
                                .variable(
                                                "legalDomain",
                                                DEFAULT_LEGAL_DOMAIN)
                                /*
                                 * PromptContext 使用 @Singular("knowledgeDocument")，
                                 * Lombok 会生成 knowledge(Collection) 方法。
                                 */
                                .knowledge(
                                                retrievalContext
                                                                .getDocuments())
                                .build();
        }

        /**
         * 保留当前 Service 的参数校验契约。
         */
        private void validateQuestion(
                        String question) {

                Objects.requireNonNull(
                                question,
                                "Question must not be null");

                if (question.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Question must not be blank");
                }
        }

        /**
         * 将空白 conversationId 统一转换为 null。
         */
        private String normalizeConversationId(
                        String conversationId) {

                if (conversationId == null
                                || conversationId.isBlank()) {
                        return null;
                }

                return conversationId.trim();
        }
}