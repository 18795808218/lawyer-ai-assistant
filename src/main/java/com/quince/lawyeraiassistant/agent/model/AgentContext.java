package com.quince.lawyeraiassistant.agent.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Agent Pipeline 的统一上下文对象。
 *
 * <p>
 * 负责保存一次 Agent 执行过程中的核心状态。
 * </p>
 *
 * <p>
 * 当前第一版包含：
 * </p>
 *
 * <ul>
 * <li>goal：Agent 需要完成的目标</li>
 * <li>status：Agent 当前执行状态</li>
 * <li>executionLogs：Agent 执行过程中的结构化日志摘要</li>
 * </ul>
 *
 * <p>
 * 本对象采用不可变设计。AgentOperator 不直接修改当前实例，
 * 而是通过 {@link #toBuilder()} 创建新的 AgentContext。
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class AgentContext {

    /**
     * Agent 需要完成的目标。
     */
    private final String goal;

    /**
     * Agent 当前执行状态。
     */
    private final AgentStatus status;

    /**
     * Agent 执行日志。
     *
     * <p>
     * 这里保存的是结构化执行摘要，不保存模型完整思维过程。
     * </p>
     */
    private final List<String> executionLogs;

    @Builder(toBuilder = true)
    private AgentContext(
            String goal,
            AgentStatus status,
            List<String> executionLogs) {

        this.goal = normalizeGoal(goal);

        this.status = status == null
                ? AgentStatus.CREATED
                : status;

        this.executionLogs = normalizeExecutionLogs(executionLogs);
    }

    /**
     * 根据 Goal 创建初始 AgentContext。
     *
     * <p>
     * 初始状态统一为 CREATED，执行日志为空集合。
     * </p>
     *
     * @param goal Agent 目标
     * @return 初始 AgentContext
     */
    public static AgentContext from(String goal) {
        return AgentContext.builder()
                .goal(goal)
                .build();
    }

    /**
     * 判断 Agent 是否已开始执行。
     */
    public boolean isRunning() {
        return status == AgentStatus.RUNNING;
    }

    /**
     * 判断 Agent 是否已经成功完成。
     */
    public boolean isFinished() {
        return status == AgentStatus.FINISHED;
    }

    /**
     * 判断 Agent 是否执行失败。
     */
    public boolean isFailed() {
        return status == AgentStatus.FAILED;
    }

    /**
     * 判断当前是否包含执行日志。
     */
    public boolean hasExecutionLogs() {
        return !executionLogs.isEmpty();
    }

    /**
     * 返回执行日志数量。
     */
    public int executionLogCount() {
        return executionLogs.size();
    }

    /**
     * 创建一个追加日志后的新 AgentContext。
     *
     * <p>
     * 原 AgentContext 不会被修改。
     * </p>
     *
     * @param executionLog 新的执行日志
     * @return 追加日志后的新 AgentContext
     */
    public AgentContext appendExecutionLog(
            String executionLog) {

        String normalizedLog = normalizeExecutionLog(executionLog);

        List<String> updatedLogs = new ArrayList<>(executionLogs);

        updatedLogs.add(normalizedLog);

        return toBuilder()
                .executionLogs(updatedLogs)
                .build();
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

    private static List<String> normalizeExecutionLogs(
            List<String> executionLogs) {

        if (executionLogs == null
                || executionLogs.isEmpty()) {
            return List.of();
        }

        List<String> normalizedLogs = executionLogs.stream()
                .map(AgentContext::normalizeExecutionLog)
                .toList();

        return List.copyOf(normalizedLogs);
    }

    private static String normalizeExecutionLog(
            String executionLog) {

        Objects.requireNonNull(
                executionLog,
                "Execution log must not be null");

        String normalizedLog = executionLog.trim();

        if (normalizedLog.isEmpty()) {
            throw new IllegalArgumentException(
                    "Execution log must not be blank");
        }

        return normalizedLog;
    }
}