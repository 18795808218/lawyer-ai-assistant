package com.quince.lawyeraiassistant.agent.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Objects;

/**
 * Agent Runtime 中的一次 Tool Action。
 *
 * <p>
 * ToolAction 表示为了执行某个 AgentTask，
 * Agent 决定调用某个 Tool，并携带对应参数。
 * </p>
 *
 * <p>
 * 注意：
 * </p>
 *
 * <ul>
 * <li>AgentTask 表示“要做什么”</li>
 * <li>ToolAction 表示“这一次具体怎么做”</li>
 * </ul>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class ToolAction {

    /**
     * 本次 Action 对应的 AgentTask ID。
     */
    private final String taskId;

    /**
     * 需要调用的 Tool 名称。
     */
    private final String toolName;

    /**
     * Tool 调用参数。
     *
     * <p>
     * 使用 Map 保存结构化参数。
     * 第一版允许 Tool 没有参数，此时统一使用空 Map。
     * </p>
     */
    private final Map<String, Object> arguments;

    @Builder(toBuilder = true)
    private ToolAction(
            String taskId,
            String toolName,
            Map<String, Object> arguments) {

        this.taskId = normalizeRequiredText(
                taskId,
                "Task id must not be null",
                "Task id must not be blank");

        this.toolName = normalizeRequiredText(
                toolName,
                "Tool name must not be null",
                "Tool name must not be blank");

        this.arguments = normalizeArguments(
                arguments);
    }

    /**
     * 创建一个 ToolAction。
     *
     * @param taskId    对应 AgentTask ID
     * @param toolName  Tool 名称
     * @param arguments Tool 参数
     * @return ToolAction
     */
    public static ToolAction of(
            String taskId,
            String toolName,
            Map<String, Object> arguments) {

        return ToolAction.builder()
                .taskId(taskId)
                .toolName(toolName)
                .arguments(arguments)
                .build();
    }

    /**
     * 创建一个无参数 ToolAction。
     */
    public static ToolAction of(
            String taskId,
            String toolName) {

        return ToolAction.builder()
                .taskId(taskId)
                .toolName(toolName)
                .build();
    }

    /**
     * 判断 Action 是否包含参数。
     */
    public boolean hasArguments() {
        return !arguments.isEmpty();
    }

    private static Map<String, Object> normalizeArguments(
            Map<String, Object> arguments) {

        if (arguments == null
                || arguments.isEmpty()) {

            return Map.of();
        }

        return Map.copyOf(
                arguments);
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
}