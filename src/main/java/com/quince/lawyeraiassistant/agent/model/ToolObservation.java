package com.quince.lawyeraiassistant.agent.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Agent Runtime 中的一次 Tool 执行观察结果。
 *
 * <p>
 * ToolObservation 用于记录某个 AgentTask 执行 Tool 后，
 * Agent Runtime 所观察到的结果。
 * </p>
 *
 * <p>
 * 注意：
 * ToolObservation 不是 Tool 的原始返回类型，
 * 而是 Agent Runtime 对 Tool Result 的统一包装。
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class ToolObservation {

    /**
     * 本次 Tool 执行对应的 AgentTask ID。
     */
    private final String taskId;

    /**
     * 本次执行使用的 Tool 名称。
     */
    private final String toolName;

    /**
     * Tool 是否执行成功。
     */
    private final boolean success;

    /**
     * Tool 成功执行后的结果内容。
     *
     * <p>
     * 失败 Observation 中允许为 null。
     * </p>
     */
    private final String content;

    /**
     * Tool 执行失败时的错误信息。
     *
     * <p>
     * 成功 Observation 中允许为 null。
     * </p>
     */
    private final String errorMessage;

    @Builder(toBuilder = true)
    private ToolObservation(
            String taskId,
            String toolName,
            boolean success,
            String content,
            String errorMessage) {

        this.taskId = normalizeRequiredText(
                taskId,
                "Task id must not be null",
                "Task id must not be blank");

        this.toolName = normalizeRequiredText(
                toolName,
                "Tool name must not be null",
                "Tool name must not be blank");

        this.success = success;

        this.content = normalizeOptionalText(
                content);

        this.errorMessage = normalizeOptionalText(
                errorMessage);

        validateState();
    }

    /**
     * 创建成功 Observation。
     *
     * @param taskId   对应任务 ID
     * @param toolName Tool 名称
     * @param content  Tool 执行结果
     * @return 成功 Observation
     */
    public static ToolObservation success(
            String taskId,
            String toolName,
            String content) {

        return ToolObservation.builder()
                .taskId(taskId)
                .toolName(toolName)
                .success(true)
                .content(content)
                .build();
    }

    /**
     * 创建失败 Observation。
     *
     * @param taskId       对应任务 ID
     * @param toolName     Tool 名称
     * @param errorMessage 错误信息
     * @return 失败 Observation
     */
    public static ToolObservation failure(
            String taskId,
            String toolName,
            String errorMessage) {

        return ToolObservation.builder()
                .taskId(taskId)
                .toolName(toolName)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 判断是否为失败 Observation。
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * 校验 Observation 状态一致性。
     *
     * <p>
     * 成功时必须有 content；
     * 失败时必须有 errorMessage。
     * </p>
     */
    private void validateState() {

        if (success) {
            if (content == null) {
                throw new IllegalArgumentException(
                        "Successful observation must contain content");
            }

            if (errorMessage != null) {
                throw new IllegalArgumentException(
                        "Successful observation must not contain errorMessage");
            }

            return;
        }

        if (errorMessage == null) {
            throw new IllegalArgumentException(
                    "Failed observation must contain errorMessage");
        }

        if (content != null) {
            throw new IllegalArgumentException(
                    "Failed observation must not contain content");
        }
    }

    private static String normalizeRequiredText(
            String value,
            String nullMessage,
            String blankMessage) {

        Objects.requireNonNull(
                value,
                nullMessage);

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    blankMessage);
        }

        return normalized;
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