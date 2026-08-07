package com.quince.lawyeraiassistant.agent.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Tool 执行结果。
 *
 * <p>
 * ToolExecutionResult 属于 Tool Execution Layer，
 * 用于描述 Tool 本身的执行结果。
 * </p>
 *
 * <p>
 * 它不同于 ToolObservation：
 * </p>
 *
 * <ul>
 * <li>ToolExecutionResult：Tool 层返回值</li>
 * <li>ToolObservation：Agent Runtime 对执行结果的记录</li>
 * </ul>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class ToolExecutionResult {

    /**
     * Tool 是否执行成功。
     */
    private final boolean success;

    /**
     * Tool 成功后的结果内容。
     */
    private final String content;

    /**
     * Tool 执行失败后的错误信息。
     */
    private final String errorMessage;

    @Builder(toBuilder = true)
    private ToolExecutionResult(
            boolean success,
            String content,
            String errorMessage) {

        this.success = success;

        this.content = normalizeOptionalText(
                content);

        this.errorMessage = normalizeOptionalText(
                errorMessage);

        validateState();
    }

    /**
     * 创建成功结果。
     */
    public static ToolExecutionResult success(
            String content) {

        return ToolExecutionResult.builder()
                .success(true)
                .content(content)
                .build();
    }

    /**
     * 创建失败结果。
     */
    public static ToolExecutionResult failure(
            String errorMessage) {

        return ToolExecutionResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 判断 Tool 是否执行失败。
     */
    public boolean isFailure() {
        return !success;
    }

    private void validateState() {

        if (success) {

            if (content == null) {
                throw new IllegalArgumentException(
                        "Successful tool result must contain content");
            }

            if (errorMessage != null) {
                throw new IllegalArgumentException(
                        "Successful tool result must not contain errorMessage");
            }

            return;
        }

        if (errorMessage == null) {
            throw new IllegalArgumentException(
                    "Failed tool result must contain errorMessage");
        }

        if (content != null) {
            throw new IllegalArgumentException(
                    "Failed tool result must not contain content");
        }
    }

    private static String normalizeOptionalText(
            String value) {

        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}