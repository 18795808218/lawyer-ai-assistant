package com.quince.lawyeraiassistant.agent.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentTaskTest {

    @Test
    void shouldCreatePendingTask() {
        AgentTask task = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        assertEquals(
                "task-1",
                task.getId());

        assertEquals(
                "读取劳动合同",
                task.getDescription());

        assertEquals(
                AgentTaskStatus.PENDING,
                task.getStatus());

        assertTrue(task.isPending());
        assertFalse(task.isRunning());
        assertFalse(task.isCompleted());
        assertFalse(task.isFailed());
    }

    @Test
    void shouldTrimIdAndDescription() {
        AgentTask task = AgentTask.pending(
                "  task-1  ",
                "  读取劳动合同  ");

        assertEquals(
                "task-1",
                task.getId());

        assertEquals(
                "读取劳动合同",
                task.getDescription());
    }

    @Test
    void shouldDefaultNullStatusToPending() {
        AgentTask task = AgentTask.builder()
                .id("task-1")
                .description("读取劳动合同")
                .status(null)
                .build();

        assertEquals(
                AgentTaskStatus.PENDING,
                task.getStatus());
    }

    @Test
    void shouldUpdateStatusWithoutModifyingOriginalTask() {
        AgentTask originalTask = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        AgentTask runningTask = originalTask.withStatus(
                AgentTaskStatus.RUNNING);

        assertNotSame(
                originalTask,
                runningTask);

        assertEquals(
                AgentTaskStatus.PENDING,
                originalTask.getStatus());

        assertEquals(
                AgentTaskStatus.RUNNING,
                runningTask.getStatus());

        assertTrue(runningTask.isRunning());
    }

    @Test
    void shouldIdentifyCompletedStatus() {
        AgentTask task = AgentTask.pending(
                "task-1",
                "读取劳动合同")
                .withStatus(
                        AgentTaskStatus.COMPLETED);

        assertTrue(task.isCompleted());
        assertFalse(task.isPending());
    }

    @Test
    void shouldIdentifyFailedStatus() {
        AgentTask task = AgentTask.pending(
                "task-1",
                "读取劳动合同")
                .withStatus(
                        AgentTaskStatus.FAILED);

        assertTrue(task.isFailed());
        assertFalse(task.isCompleted());
    }

    @Test
    void shouldRejectNullId() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> AgentTask.pending(
                        null,
                        "读取劳动合同"));

        assertEquals(
                "Task id must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AgentTask.pending(
                        "   ",
                        "读取劳动合同"));

        assertEquals(
                "Task id must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullDescription() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> AgentTask.pending(
                        "task-1",
                        null));

        assertEquals(
                "Task description must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankDescription() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AgentTask.pending(
                        "task-1",
                        "   "));

        assertEquals(
                "Task description must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullStatusInHelperMethod() {
        AgentTask task = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> task.withStatus(null));

        assertEquals(
                "AgentTaskStatus must not be null",
                exception.getMessage());
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        AgentTask first = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        AgentTask second = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        assertEquals(first, second);

        assertEquals(
                first.hashCode(),
                second.hashCode());
    }
}