package com.quince.lawyeraiassistant.prompt.template;

import com.quince.lawyeraiassistant.prompt.knowledge.KnowledgeFormatter;
import com.quince.lawyeraiassistant.prompt.model.PromptContext;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Prompt 模板变量值对象。
 *
 * <p>
 * 负责把 PromptContext 中的数据转换成模板引擎可以使用的变量。
 * </p>
 *
 * <p>
 * 当前核心变量：
 * </p>
 * <ul>
 * <li>question：用户问题</li>
 * <li>knowledge：格式化后的 RAG 知识</li>
 * <li>conversationId：会话编号</li>
 * </ul>
 */
@Getter
@Builder
public class TemplateVariables {

    public static final String QUESTION = "question";

    public static final String KNOWLEDGE = "knowledge";

    public static final String CONVERSATION_ID = "conversationId";

    /**
     * 用户问题。
     */
    private final String question;

    /**
     * 已格式化的 RAG 知识文本。
     */
    private final String knowledge;

    /**
     * 当前会话编号。
     */
    private final String conversationId;

    /**
     * 额外模板变量。
     *
     * <p>
     * 扩展变量不能覆盖核心变量。
     * </p>
     */
    @Builder.Default
    private final Map<String, Object> additionalVariables = Collections.emptyMap();

    /**
     * 从 PromptContext 创建模板变量。
     *
     * <p>
     * 该方法负责调用 KnowledgeFormatter，把
     * PromptContext 中的 List&lt;Document&gt; 转换成 Markdown 文本。
     * </p>
     *
     * @param context            Prompt 构建上下文
     * @param knowledgeFormatter RAG 知识格式化器
     * @return 模板变量
     */
    public static TemplateVariables from(
            PromptContext context,
            KnowledgeFormatter knowledgeFormatter) {
        Objects.requireNonNull(
                context,
                "PromptContext must not be null");

        Objects.requireNonNull(
                knowledgeFormatter,
                "KnowledgeFormatter must not be null");

        String formattedKnowledge = knowledgeFormatter.format(
                context.safeKnowledge());

        return TemplateVariables.builder()
                .question(
                        defaultString(
                                context.getQuestion()))
                .knowledge(
                        defaultString(
                                formattedKnowledge))
                .conversationId(
                        defaultString(
                                context.getConversationId()))
                .additionalVariables(
                        context.safeVariables())
                .build();
    }

    /**
     * 转换为模板引擎所需的变量 Map。
     *
     * <p>
     * 三个核心变量始终存在。即使值不存在，也会使用空字符串，
     * 防止 PromptTemplate 因变量缺失而渲染失败。
     * </p>
     *
     * @return 不可修改的变量 Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> variables = new HashMap<>();

        variables.put(
                QUESTION,
                defaultString(question));

        variables.put(
                KNOWLEDGE,
                defaultString(knowledge));

        variables.put(
                CONVERSATION_ID,
                defaultString(conversationId));

        addAdditionalVariables(variables);

        return Collections.unmodifiableMap(variables);
    }

    /**
     * 添加扩展变量，但不允许覆盖核心变量。
     */
    private void addAdditionalVariables(
            Map<String, Object> variables) {
        if (additionalVariables == null
                || additionalVariables.isEmpty()) {
            return;
        }

        additionalVariables.forEach(
                (key, value) -> {
                    if (isInvalidKey(key)
                            || value == null
                            || variables.containsKey(key)) {
                        return;
                    }

                    variables.put(key, value);
                });
    }

    private boolean isInvalidKey(String key) {
        return key == null || key.isBlank();
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }
}