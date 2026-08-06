package com.quince.lawyeraiassistant.agent.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Agent Planning 阶段生成的执行计划。
 *
 * <p>
 * 一个 AgentPlan 由多个有序的 AgentTask 组成。
 * Task 在列表中的顺序即默认执行顺序。
 * </p>
 *
 * <p>
 * 本对象采用不可变设计，内部任务列表始终保存为不可修改快照。
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class AgentPlan {

    /**
     * 按执行顺序排列的任务列表。
     */
    private final List<AgentTask> tasks;

    @Builder(toBuilder = true)
    private AgentPlan(
            List<AgentTask> tasks) {

        this.tasks = normalizeTasks(tasks);
    }

    /**
     * 根据任务列表创建 AgentPlan。
     *
     * @param tasks 任务列表
     * @return AgentPlan
     */
    public static AgentPlan from(
            List<AgentTask> tasks) {

        return AgentPlan.builder()
                .tasks(tasks)
                .build();
    }

    /**
     * 创建空计划。
     */
    public static AgentPlan empty() {
        return AgentPlan.builder()
                .tasks(List.of())
                .build();
    }

    /**
     * 判断计划中是否包含任务。
     */
    public boolean hasTasks() {
        return !tasks.isEmpty();
    }

    /**
     * 返回任务数量。
     */
    public int taskCount() {
        return tasks.size();
    }

    /**
     * 判断全部任务是否都已完成。
     *
     * <p>
     * 空计划不视为完成。
     * </p>
     */
    public boolean isCompleted() {
        return hasTasks()
                && tasks.stream()
                        .allMatch(AgentTask::isCompleted);
    }

    /**
     * 判断计划中是否存在失败任务。
     */
    public boolean hasFailedTask() {
        return tasks.stream()
                .anyMatch(AgentTask::isFailed);
    }

    /**
     * 获取指定 ID 的任务。
     */
    public Optional<AgentTask> findTaskById(
            String taskId) {

        Objects.requireNonNull(
                taskId,
                "Task id must not be null");

        String normalizedTaskId = taskId.trim();

        if (normalizedTaskId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Task id must not be blank");
        }

        return tasks.stream()
                .filter(task -> task.getId()
                        .equals(normalizedTaskId))
                .findFirst();
    }

    /**
     * 获取下一个待执行任务。
     *
     * <p>
     * 第一版按照任务列表顺序返回第一个 PENDING Task。
     * </p>
     */
    public Optional<AgentTask> nextPendingTask() {
        return tasks.stream()
                .filter(AgentTask::isPending)
                .findFirst();
    }

    /**
     * 更新指定任务状态，并返回新的 AgentPlan。
     *
     * @param taskId    任务 ID
     * @param newStatus 新状态
     * @return 新 AgentPlan
     */
    public AgentPlan updateTaskStatus(
            String taskId,
            AgentTaskStatus newStatus) {

        Objects.requireNonNull(
                newStatus,
                "AgentTaskStatus must not be null");

        AgentTask existingTask = findTaskById(taskId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Task not found: "
                                        + taskId.trim()));

        List<AgentTask> updatedTasks = new ArrayList<>(tasks.size());

        for (AgentTask task : tasks) {
            if (task.getId()
                    .equals(existingTask.getId())) {

                updatedTasks.add(
                        task.withStatus(newStatus));
            } else {
                updatedTasks.add(task);
            }
        }

        return toBuilder()
                .tasks(updatedTasks)
                .build();
    }

    private static List<AgentTask> normalizeTasks(
            List<AgentTask> tasks) {

        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }

        List<AgentTask> copiedTasks = List.copyOf(tasks);

        validateUniqueTaskIds(copiedTasks);

        return copiedTasks;
    }

    /**
     * 一个 Plan 中不允许出现重复 Task ID。
     */
    private static void validateUniqueTaskIds(
            List<AgentTask> tasks) {

        long distinctIdCount = tasks.stream()
                .map(AgentTask::getId)
                .distinct()
                .count();

        if (distinctIdCount != tasks.size()) {
            throw new IllegalArgumentException(
                    "AgentPlan must not contain duplicate task ids");
        }
    }
}