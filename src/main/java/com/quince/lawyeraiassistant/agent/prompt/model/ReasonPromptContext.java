package com.quince.lawyeraiassistant.agent.prompt.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Objects;

/**
 * Agent Reason Prompt 的动态上下文。
 *
 * <p>
 * 本对象只保存构建 Reason Prompt 所需要的数据，
 * 不直接依赖完整的 AgentContext，避免 Prompt 层与
 * Agent Runtime 产生过强耦合。
 * </p>
 *
 * <p>
 * 当前第一版只包含 Goal。后续可以继续扩展：
 * </p>
 *
 * <ul>
 * <li>conversationId</li>
 * <li>tenantId</li>
 * <li>language</li>
 * <li>memorySummary</li>
 * <li>executionHistory</li>
 * </ul>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class ReasonPromptContext {

    /**
     * Agent 需要理解的用户目标。
     */
    private final String goal;

    @Builder(toBuilder = true)
    private ReasonPromptContext(
            String goal) {

        this.goal = normalizeGoal(goal);
    }

    /**
     * 根据 Goal 创建 ReasonPromptContext。
     *
     * @param goal Agent 目标
     * @return Reason Prompt 上下文
     */
    public static ReasonPromptContext from(
            String goal) {

        return ReasonPromptContext.builder()
                .goal(goal)
                .build();
    }

    /**
     * 转换为模板渲染变量。
     *
     * @return 不可修改的模板变量 Map
     */
    public Map<String, Object> toVariables() {
        return Map.of(
                "goal",
                goal);
    }

    private static String normalizeGoal(
            String goal) {

        Objects.requireNonNull(
                goal,
                "Goal must not be null");

        String normalizedGoal = goal.trim();

        if (normalizedGoal.isEmpty()) {
            throw new IllegalArgumentException(
                    "Goal must not be blank");
        }

        return normalizedGoal;
    }
}