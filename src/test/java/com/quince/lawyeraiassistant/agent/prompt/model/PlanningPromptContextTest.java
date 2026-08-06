package com.quince.lawyeraiassistant.agent.prompt.model;

import com.quince.lawyeraiassistant.agent.model.ReasonResult;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlanningPromptContextTest {

    @Test
    void shouldCreatePlanningPromptContext() {
        ReasonResult reasonResult = ReasonResult.from(
                "用户希望分析劳动合同。");

        PlanningPromptContext context = PlanningPromptContext.from(
                "分析劳动合同",
                reasonResult);

        assertEquals(
                "分析劳动合同",
                context.getGoal());

        assertEquals(
                reasonResult,
                context.getReasonResult());

        assertEquals(
                Map.of(
                        "goal",
                        "分析劳动合同",
                        "reasonSummary",
                        "用户希望分析劳动合同。"),
                context.toVariables());
    }

    @Test
    void shouldTrimGoal() {
        PlanningPromptContext context = PlanningPromptContext.from(
                "  分析劳动合同  ",
                ReasonResult.from(
                        "用户希望分析劳动合同。"));

        assertEquals(
                "分析劳动合同",
                context.getGoal());
    }

    @Test
    void shouldRejectNullGoal() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PlanningPromptContext.from(
                        null,
                        ReasonResult.from(
                                "测试")));

        assertEquals(
                "Goal must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankGoal() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PlanningPromptContext.from(
                        "   ",
                        ReasonResult.from(
                                "测试")));

        assertEquals(
                "Goal must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullReasonResult() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> PlanningPromptContext.from(
                        "分析劳动合同",
                        null));

        assertEquals(
                "ReasonResult must not be null",
                exception.getMessage());
    }
}