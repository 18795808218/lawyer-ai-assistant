package com.quince.lawyeraiassistant.agent.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentPlanTest {

    @Test
    void shouldCreatePlanFromTasks() {
        AgentTask firstTask = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        AgentTask secondTask = AgentTask.pending(
                "task-2",
                "分析法律风险");

        AgentPlan plan = AgentPlan.from(
                List.of(
                        firstTask,
                        secondTask));

        assertTrue(plan.hasTasks());

        assertEquals(
                2,
                plan.taskCount());

        assertEquals(
                List.of(
                        firstTask,
                        secondTask),
                plan.getTasks());
    }

    @Test
    void shouldCreateEmptyPlan() {
        AgentPlan plan = AgentPlan.empty();

        assertFalse(plan.hasTasks());

        assertEquals(
                0,
                plan.taskCount());

        assertFalse(plan.isCompleted());

        assertTrue(
                plan.nextPendingTask()
                        .isEmpty());
    }

    @Test
    void shouldNormalizeNullTasksToEmptyList() {
        AgentPlan plan = AgentPlan.builder()
                .tasks(null)
                .build();

        assertTrue(
                plan.getTasks()
                        .isEmpty());
    }

    @Test
    void shouldCreateDefensiveCopyOfTasks() {
        List<AgentTask> mutableTasks = new ArrayList<>();

        mutableTasks.add(
                AgentTask.pending(
                        "task-1",
                        "读取劳动合同"));

        AgentPlan plan = AgentPlan.from(
                mutableTasks);

        mutableTasks.clear();

        assertEquals(
                1,
                plan.taskCount());
    }

    @Test
    void shouldExposeUnmodifiableTaskList() {
        AgentPlan plan = AgentPlan.from(
                List.of(
                        AgentTask.pending(
                                "task-1",
                                "读取劳动合同")));

        assertThrows(
                UnsupportedOperationException.class,
                () -> plan.getTasks()
                        .add(
                                AgentTask.pending(
                                        "task-2",
                                        "分析风险")));
    }

    @Test
    void shouldFindTaskById() {
        AgentTask task = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        AgentPlan plan = AgentPlan.from(
                List.of(task));

        assertTrue(
                plan.findTaskById(
                        "  task-1  ")
                        .isPresent());

        assertEquals(
                task,
                plan.findTaskById(
                        "task-1")
                        .orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenTaskDoesNotExist() {
        AgentPlan plan = AgentPlan.from(
                List.of(
                        AgentTask.pending(
                                "task-1",
                                "读取劳动合同")));

        assertTrue(
                plan.findTaskById(
                        "task-999")
                        .isEmpty());
    }

    @Test
    void shouldReturnFirstPendingTask() {
        AgentTask completedTask = AgentTask.pending(
                "task-1",
                "读取劳动合同")
                .withStatus(
                        AgentTaskStatus.COMPLETED);

        AgentTask pendingTask = AgentTask.pending(
                "task-2",
                "分析法律风险");

        AgentPlan plan = AgentPlan.from(
                List.of(
                        completedTask,
                        pendingTask));

        assertEquals(
                pendingTask,
                plan.nextPendingTask()
                        .orElseThrow());
    }

    @Test
    void shouldUpdateTaskStatusWithoutModifyingOriginalPlan() {
        AgentTask task = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        AgentPlan originalPlan = AgentPlan.from(
                List.of(task));

        AgentPlan updatedPlan = originalPlan.updateTaskStatus(
                "task-1",
                AgentTaskStatus.RUNNING);

        assertNotSame(
                originalPlan,
                updatedPlan);

        assertEquals(
                AgentTaskStatus.PENDING,
                originalPlan.getTasks()
                        .getFirst()
                        .getStatus());

        assertEquals(
                AgentTaskStatus.RUNNING,
                updatedPlan.getTasks()
                        .getFirst()
                        .getStatus());
    }

    @Test
    void shouldIdentifyCompletedPlan() {
        AgentPlan plan = AgentPlan.from(
                List.of(
                        AgentTask.pending(
                                "task-1",
                                "读取劳动合同")
                                .withStatus(
                                        AgentTaskStatus.COMPLETED),
                        AgentTask.pending(
                                "task-2",
                                "分析法律风险")
                                .withStatus(
                                        AgentTaskStatus.COMPLETED)));

        assertTrue(
                plan.isCompleted());

        assertFalse(
                plan.hasFailedTask());
    }

    @Test
    void shouldIdentifyFailedTask() {
        AgentPlan plan = AgentPlan.from(
                List.of(
                        AgentTask.pending(
                                "task-1",
                                "读取劳动合同")
                                .withStatus(
                                        AgentTaskStatus.FAILED)));

        assertTrue(
                plan.hasFailedTask());

        assertFalse(
                plan.isCompleted());
    }

    @Test
    void shouldRejectDuplicateTaskIds() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AgentPlan.from(
                        List.of(
                                AgentTask.pending(
                                        "task-1",
                                        "读取劳动合同"),
                                AgentTask.pending(
                                        "task-1",
                                        "分析法律风险"))));

        assertEquals(
                "AgentPlan must not contain duplicate task ids",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullElementInTaskList() {
        List<AgentTask> tasks = new ArrayList<>();

        tasks.add(
                AgentTask.pending(
                        "task-1",
                        "读取劳动合同"));

        tasks.add(null);

        assertThrows(
                NullPointerException.class,
                () -> AgentPlan.from(tasks));
    }

    @Test
    void shouldRejectNullTaskIdWhenFindingTask() {
        AgentPlan plan = AgentPlan.empty();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> plan.findTaskById(null));

        assertEquals(
                "Task id must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankTaskIdWhenFindingTask() {
        AgentPlan plan = AgentPlan.empty();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> plan.findTaskById("   "));

        assertEquals(
                "Task id must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectUnknownTaskWhenUpdatingStatus() {
        AgentPlan plan = AgentPlan.empty();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> plan.updateTaskStatus(
                        "task-999",
                        AgentTaskStatus.RUNNING));

        assertEquals(
                "Task not found: task-999",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullStatusWhenUpdatingTask() {
        AgentPlan plan = AgentPlan.from(
                List.of(
                        AgentTask.pending(
                                "task-1",
                                "读取劳动合同")));

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> plan.updateTaskStatus(
                        "task-1",
                        null));

        assertEquals(
                "AgentTaskStatus must not be null",
                exception.getMessage());
    }
}