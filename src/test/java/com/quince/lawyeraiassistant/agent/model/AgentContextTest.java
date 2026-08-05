package com.quince.lawyeraiassistant.agent.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentContextTest {

    @Test
    void shouldCreateInitialContextFromGoal() {
        AgentContext context = AgentContext.from(
                "分析劳动合同并生成律师意见书");

        assertEquals(
                "分析劳动合同并生成律师意见书",
                context.getGoal());

        assertEquals(
                AgentStatus.CREATED,
                context.getStatus());

        assertTrue(
                context.getExecutionLogs()
                        .isEmpty());

        assertFalse(context.isRunning());
        assertFalse(context.isFinished());
        assertFalse(context.isFailed());
        assertFalse(context.hasExecutionLogs());

        assertEquals(
                0,
                context.executionLogCount());
    }

    @Test
    void shouldTrimGoal() {
        AgentContext context = AgentContext.from(
                "  分析劳动合同  ");

        assertEquals(
                "分析劳动合同",
                context.getGoal());
    }

    @Test
    void shouldDefaultNullStatusToCreated() {
        AgentContext context = AgentContext.builder()
                .goal("测试目标")
                .status(null)
                .build();

        assertEquals(
                AgentStatus.CREATED,
                context.getStatus());
    }

    @Test
    void shouldNormalizeNullLogsToEmptyList() {
        AgentContext context = AgentContext.builder()
                .goal("测试目标")
                .executionLogs(null)
                .build();

        assertTrue(
                context.getExecutionLogs()
                        .isEmpty());
    }

    @Test
    void shouldCreateRunningContextWithToBuilder() {
        AgentContext originalContext = AgentContext.from(
                "分析劳动合同");

        AgentContext runningContext = originalContext.toBuilder()
                .status(
                        AgentStatus.RUNNING)
                .build();

        assertNotSame(
                originalContext,
                runningContext);

        assertEquals(
                AgentStatus.CREATED,
                originalContext.getStatus());

        assertEquals(
                AgentStatus.RUNNING,
                runningContext.getStatus());

        assertTrue(
                runningContext.isRunning());

        assertEquals(
                originalContext.getGoal(),
                runningContext.getGoal());
    }

    @Test
    void shouldAppendExecutionLogWithoutModifyingOriginal() {
        AgentContext originalContext = AgentContext.from(
                "分析劳动合同");

        AgentContext updatedContext = originalContext.appendExecutionLog(
                "  Reason completed  ");

        assertNotSame(
                originalContext,
                updatedContext);

        assertTrue(
                originalContext
                        .getExecutionLogs()
                        .isEmpty());

        assertEquals(
                List.of(
                        "Reason completed"),
                updatedContext
                        .getExecutionLogs());

        assertTrue(
                updatedContext.hasExecutionLogs());

        assertEquals(
                1,
                updatedContext
                        .executionLogCount());
    }

    @Test
    void shouldPreserveExistingLogsWhenAppending() {
        AgentContext context = AgentContext.builder()
                .goal("生成律师意见书")
                .executionLogs(
                        List.of(
                                "Reason completed"))
                .build();

        AgentContext result = context.appendExecutionLog(
                "Planning completed");

        assertEquals(
                List.of(
                        "Reason completed",
                        "Planning completed"),
                result.getExecutionLogs());
    }

    @Test
    void shouldCreateDefensiveCopyOfExecutionLogs() {
        List<String> mutableLogs = new ArrayList<>();

        mutableLogs.add(
                "Reason completed");

        AgentContext context = AgentContext.builder()
                .goal("测试目标")
                .executionLogs(mutableLogs)
                .build();

        mutableLogs.clear();

        assertEquals(
                List.of(
                        "Reason completed"),
                context.getExecutionLogs());
    }

    @Test
    void shouldExposeUnmodifiableExecutionLogs() {
        AgentContext context = AgentContext.builder()
                .goal("测试目标")
                .executionLogs(
                        List.of(
                                "Reason completed"))
                .build();

        assertThrows(
                UnsupportedOperationException.class,
                () -> context
                        .getExecutionLogs()
                        .add(
                                "Illegal log"));
    }

    @Test
    void shouldIdentifyFinishedStatus() {
        AgentContext context = AgentContext.builder()
                .goal("测试目标")
                .status(
                        AgentStatus.FINISHED)
                .build();

        assertTrue(context.isFinished());
        assertFalse(context.isRunning());
        assertFalse(context.isFailed());
    }

    @Test
    void shouldIdentifyFailedStatus() {
        AgentContext context = AgentContext.builder()
                .goal("测试目标")
                .status(
                        AgentStatus.FAILED)
                .build();

        assertTrue(context.isFailed());
        assertFalse(context.isRunning());
        assertFalse(context.isFinished());
    }

    @Test
    void shouldRejectNullGoal() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> AgentContext.from(null));

        assertEquals(
                "Goal must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankGoal() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AgentContext.from("   "));

        assertEquals(
                "Goal must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullExecutionLog() {
        AgentContext context = AgentContext.from(
                "测试目标");

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> context
                        .appendExecutionLog(
                                null));

        assertEquals(
                "Execution log must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankExecutionLog() {
        AgentContext context = AgentContext.from(
                "测试目标");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context
                        .appendExecutionLog(
                                "   "));

        assertEquals(
                "Execution log must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullElementInExecutionLogs() {
        List<String> logs = new ArrayList<>();

        logs.add(
                "Reason completed");

        logs.add(null);

        assertThrows(
                NullPointerException.class,
                () -> AgentContext.builder()
                        .goal("测试目标")
                        .executionLogs(logs)
                        .build());
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        AgentContext first = AgentContext.builder()
                .goal("分析劳动合同")
                .status(
                        AgentStatus.RUNNING)
                .executionLogs(
                        List.of(
                                "Reason completed"))
                .build();

        AgentContext second = AgentContext.builder()
                .goal("分析劳动合同")
                .status(
                        AgentStatus.RUNNING)
                .executionLogs(
                        List.of(
                                "Reason completed"))
                .build();

        assertEquals(first, second);

        assertEquals(
                first.hashCode(),
                second.hashCode());
    }
}