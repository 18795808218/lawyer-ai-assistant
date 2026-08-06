package com.quince.lawyeraiassistant.agent.prompt.model;

import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;
import java.util.Objects;

/**
 * Agent Planning Prompt 的动态上下文。
 *
 * <p>
 * Planning 阶段根据 Goal 和 ReasonResult 生成结构化 AgentPlan。
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class PlanningPromptContext {

    private final String goal;

    private final ReasonResult reasonResult;

    @Builder(toBuilder = true)
    private PlanningPromptContext(
            String goal,
            ReasonResult reasonResult) {

        this.goal = normalizeGoal(goal);

        this.reasonResult = Objects.requireNonNull(
                reasonResult,
                "ReasonResult must not be null");
    }

    public static PlanningPromptContext from(
            String goal,
            ReasonResult reasonResult) {

        return PlanningPromptContext.builder()
                .goal(goal)
                .reasonResult(reasonResult)
                .build();
    }

    public Map<String, Object> toVariables() {
        return Map.of(
                "goal",
                goal,
                "reasonSummary",
                reasonResult.getReasonSummary());
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