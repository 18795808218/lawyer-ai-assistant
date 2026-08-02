package com.quince.lawyeraiassistant.rag.service;

import com.quince.lawyeraiassistant.prompt.builder.LegalPromptBuilder;
import com.quince.lawyeraiassistant.prompt.model.PromptContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 法律 RAG 问答服务。
 *
 * <p>
 * 当前职责：
 * </p>
 *
 * <ul>
 * <li>接收用户问题和可选会话编号</li>
 * <li>创建 PromptContext</li>
 * <li>委托 LegalPromptBuilder 构建 Prompt</li>
 * <li>调用 ragChatClient 获取模型回答</li>
 * </ul>
 *
 * <p>
 * 本类不负责：
 * </p>
 *
 * <ul>
 * <li>读取 Prompt 文件</li>
 * <li>渲染 PromptTemplate</li>
 * <li>格式化 RAG Document</li>
 * <li>拼接 SystemMessage 和 UserMessage</li>
 * </ul>
 */
@Service
public class RagChatService {

    private static final String DEFAULT_LEGAL_DOMAIN = "劳动法";

    private final ChatClient ragChatClient;

    private final LegalPromptBuilder legalPromptBuilder;

    public RagChatService(
            @Qualifier("ragChatClient") ChatClient ragChatClient,
            LegalPromptBuilder legalPromptBuilder) {
        this.ragChatClient = Objects.requireNonNull(
                ragChatClient,
                "ragChatClient must not be null");

        this.legalPromptBuilder = Objects.requireNonNull(
                legalPromptBuilder,
                "legalPromptBuilder must not be null");
    }

    /**
     * 执行无会话编号的法律问答。
     *
     * <p>
     * 保留该方法是为了兼容当前 Controller，
     * 调用方暂时不需要传递 conversationId。
     * </p>
     *
     * @param question 用户问题
     * @return 模型回答
     */
    public String chat(String question) {
        return chat(question, null);
    }

    /**
     * 执行带会话编号的法律问答。
     *
     * <p>
     * 当前 conversationId 会进入 PromptContext。
     * 后续接入 ChatMemory 和 AI Trace 时可直接复用。
     * </p>
     *
     * @param question       用户问题
     * @param conversationId 会话编号，可以为空
     * @return 模型回答
     */
    public String chat(
            String question,
            String conversationId) {
        PromptContext context = createPromptContext(
                question,
                conversationId);

        Prompt prompt = legalPromptBuilder.build(context);

        return ragChatClient
                .prompt(prompt)
                .call()
                .content();
    }

    /**
     * 创建一次法律问答所需要的 PromptContext。
     */
    private PromptContext createPromptContext(
            String question,
            String conversationId) {
        validateQuestion(question);

        return PromptContext.builder()
                .question(question)
                .conversationId(
                        normalizeConversationId(
                                conversationId))
                .variable(
                        "legalDomain",
                        DEFAULT_LEGAL_DOMAIN)
                .build();
    }

    /**
     * 校验用户问题。
     */
    private void validateQuestion(String question) {
        Objects.requireNonNull(
                question,
                "Question must not be null");

        if (question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be blank");
        }
    }

    /**
     * 将空白会话编号统一转换为 null。
     *
     * <p>
     * 避免系统中同时出现 null、空字符串和纯空格三种
     * “没有会话编号”的状态。
     * </p>
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