package com.quince.lawyeraiassistant.query.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Query Pipeline 的上下文对象。
 *
 * <p>
 * 负责保存一次查询在 Query Pipeline 中流转的数据。
 * </p>
 *
 * <p>
 * 当前 Sprint 1 支持：
 * </p>
 * <ul>
 * <li>question：用户原始问题</li>
 * <li>conversationId：可选的会话编号</li>
 * <li>rewriteQuery：改写后的检索查询</li>
 * </ul>
 *
 * <p>
 * 该对象采用不可变设计。Transformer 不直接修改当前实例，
 * 而是通过 {@link #toBuilder()} 创建一个新的 QueryContext。
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class QueryContext {

    /**
     * 用户提交的原始问题。
     *
     * <p>
     * 该字段不能为空，也不会被 Rewrite 覆盖。
     * </p>
     */
    private final String question;

    /**
     * 当前会话编号。
     *
     * <p>
     * 用于后续 Conversation Rewrite 和 ChatMemory。
     * </p>
     */
    private final String conversationId;

    /**
     * 改写后、适合知识库检索的 Query。
     *
     * <p>
     * 在 RewriteTransformer 执行之前可以为空。
     * </p>
     */
    private final String rewriteQuery;

    /**
     * 创建 QueryContext。
     *
     * <p>
     * {@code toBuilder = true} 允许 Transformer 在保留原始数据的
     * 基础上构建新实例。
     * </p>
     */
    @Builder(toBuilder = true)
    private QueryContext(
            String question,
            String conversationId,
            String rewriteQuery) {
        this.question = validateQuestion(question);

        this.conversationId = normalizeOptionalText(conversationId);

        this.rewriteQuery = normalizeOptionalText(rewriteQuery);
    }

    /**
     * 根据原始问题创建初始 QueryContext。
     *
     * @param question 用户问题
     * @return 初始查询上下文
     */
    public static QueryContext from(String question) {
        return from(question, null);
    }

    /**
     * 根据问题和会话编号创建初始 QueryContext。
     *
     * @param question       用户问题
     * @param conversationId 会话编号，可以为空
     * @return 初始查询上下文
     */
    public static QueryContext from(
            String question,
            String conversationId) {
        return QueryContext.builder()
                .question(question)
                .conversationId(conversationId)
                .build();
    }

    /**
     * 判断当前上下文是否包含会话编号。
     */
    public boolean hasConversationId() {
        return conversationId != null;
    }

    /**
     * 判断 Rewrite 是否已经产生结果。
     */
    public boolean hasRewriteQuery() {
        return rewriteQuery != null;
    }

    /**
     * 获取当前应该用于检索的 Query。
     *
     * <p>
     * 如果 Rewrite 已完成，则返回 rewriteQuery；
     * 否则安全回退到用户原始问题。
     * </p>
     *
     * @return 当前有效检索 Query
     */
    public String effectiveQuery() {
        return hasRewriteQuery()
                ? rewriteQuery
                : question;
    }

    private static String validateQuestion(
            String question) {
        Objects.requireNonNull(
                question,
                "Query question must not be null");

        String normalizedQuestion = question.trim();

        if (normalizedQuestion.isEmpty()) {
            throw new IllegalArgumentException(
                    "Query question must not be blank");
        }

        return normalizedQuestion;
    }

    /**
     * 将可选文本中的 null、空字符串和纯空格统一转换为 null。
     */
    private static String normalizeOptionalText(
            String value) {
        if (value == null) {
            return null;
        }

        String normalizedValue = value.trim();

        return normalizedValue.isEmpty()
                ? null
                : normalizedValue;
    }
}