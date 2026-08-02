package com.quince.lawyeraiassistant.prompt.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.springframework.ai.document.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 表示一次 Prompt 构建过程所需要的动态上下文。
 *
 * <p>
 * PromptContext 不负责保存静态 Prompt 模板，
 * 静态模板由 PromptFragment 表示。
 * </p>
 *
 * <p>
 * 它主要保存每一次请求都会发生变化的数据，例如：
 * </p>
 * <ul>
 * <li>用户问题</li>
 * <li>RAG 检索得到的知识</li>
 * <li>会话 ID</li>
 * <li>模板渲染变量</li>
 * </ul>
 */
@Getter
@Builder
@ToString
public class PromptContext {

    /**
     * 用户当前提出的问题。
     */
    private final String question;

    /**
     * RAG 检索得到的相关知识文档。
     *
     * <p>
     * 这里直接使用 Spring AI 的 Document，
     * 因为 Retriever 和 VectorStore 的返回结果本身就是
     * List&lt;Document&gt;。
     * </p>
     */
    @Singular("knowledgeDocument")
    private final List<Document> knowledge;

    /**
     * 当前会话的唯一标识。
     *
     * <p>
     * 后续可以用来接入 ChatMemory、Conversation Summary
     * 或日志追踪。
     * </p>
     */
    private final String conversationId;

    /**
     * PromptTemplate 渲染时使用的动态变量。
     *
     * <p>
     * 例如：
     * </p>
     * 
     * <pre>
     * legalDomain = 劳动法
     * language = zh-CN
     * outputFormat = markdown
     * </pre>
     */
    @Singular("variable")
    private final Map<String, Object> variables;

    /**
     * 返回安全的知识文档列表。
     *
     * <p>
     * 当 Builder 没有传入 knowledge 时，
     * Lombok 生成的字段可能为 null。通过该方法统一返回空列表，
     * 避免后续 LegalPromptBuilder 到处进行 null 判断。
     * </p>
     */
    public List<Document> safeKnowledge() {
        if (knowledge == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(knowledge);
    }

    /**
     * 返回安全的 Prompt 变量集合。
     *
     * <p>
     * 当没有传入变量时返回空 Map。
     * </p>
     */
    public Map<String, Object> safeVariables() {
        if (variables == null) {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(variables);
    }

    /**
     * 判断当前上下文是否包含 RAG 检索知识。
     */
    public boolean hasKnowledge() {
        return knowledge != null && !knowledge.isEmpty();
    }

    /**
     * 判断当前上下文是否包含会话 ID。
     */
    public boolean hasConversationId() {
        return conversationId != null && !conversationId.isBlank();
    }
}