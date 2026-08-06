package com.quince.lawyeraiassistant.agent.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Agent 执行计划中的单个任务。
 *
 * <p>
 * 当前第一版包含：
 * </p>
 *
 * <ul>
 * <li>id：任务唯一标识</li>
 * <li>description：任务描述</li>
 * <li>status：任务当前状态</li>
 * </ul>
 *
 * <p>
 * 本对象采用不可变设计。任务状态变化时，
 * 通过 {@link #toBuilder()} 或辅助方法创建新实例。
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class AgentTask {

    /**
     * 任务唯一标识。
     *
     * <p>
     * 第一版可使用：
     * </p>
     *
     * <pre>
     * task-1
     * task-2
     * task-3
     * </pre>
     */
    private final String id;

    /**
     * 任务描述。
     */
    private final String description;

    /**
     * 任务当前执行状态。
     */
    private final AgentTaskStatus status;

    @Builder(toBuilder = true)
    private AgentTask(
            String id,
            String description,
            AgentTaskStatus status) {

        this.id = normalizeId(id);

        this.description = normalizeDescription(description);

        this.status = status == null
                ? AgentTaskStatus.PENDING
                : status;
    }

    /**
     * 创建一个初始状态为 PENDING 的 AgentTask。
     *
     * @param id          任务标识
     * @param description 任务描述
     * @return AgentTask
     */
    public static AgentTask pending(
            String id,
            String description) {

        return AgentTask.builder()
                .id(id)
                .description(description)
                .status(AgentTaskStatus.PENDING)
                .build();
    }

    /**
     * 判断任务是否尚未开始。
     */
    public boolean isPending() {
        return status == AgentTaskStatus.PENDING;
    }

    /**
     * 判断任务是否正在执行。
     */
    public boolean isRunning() {
        return status == AgentTaskStatus.RUNNING;
    }

    /**
     * 判断任务是否已经完成。
     */
    public boolean isCompleted() {
        return status == AgentTaskStatus.COMPLETED;
    }

    /**
     * 判断任务是否执行失败。
     */
    public boolean isFailed() {
        return status == AgentTaskStatus.FAILED;
    }

    /**
     * 返回状态更新后的新任务。
     *
     * @param newStatus 新状态
     * @return 新 AgentTask
     */
    public AgentTask withStatus(
            AgentTaskStatus newStatus) {

        Objects.requireNonNull(
                newStatus,
                "AgentTaskStatus must not be null");

        return toBuilder()
                .status(newStatus)
                .build();
    }

    private static String normalizeId(
            String id) {

        Objects.requireNonNull(
                id,
                "Task id must not be null");

        String normalizedId = id.trim();

        if (normalizedId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Task id must not be blank");
        }

        return normalizedId;
    }

    private static String normalizeDescription(
            String description) {

        Objects.requireNonNull(
                description,
                "Task description must not be null");

        String normalizedDescription = description.trim();

        if (normalizedDescription.isEmpty()) {
            throw new IllegalArgumentException(
                    "Task description must not be blank");
        }

        return normalizedDescription;
    }
}