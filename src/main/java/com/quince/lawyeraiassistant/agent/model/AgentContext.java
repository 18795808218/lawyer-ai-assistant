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
 * 当前版本包含：
 * </p>
 *
 * <ul>
 * <li>goal：Agent 需要完成的目标</li>
 * <li>reasonResult：Reason 阶段产生的推理摘要结果</li>
 * <li>agentPlan：Planning 阶段产生的执行计划</li>
 * <li>status：Agent 当前执行状态</li>
 * <li>executionLogs：Agent 执行过程中的结构化日志摘要</li>
 * </ul>
 *
 * <p>
 * 本对象采用不可变设计。AgentOperator 不直接修改当前实例，
 * 而是通过 {@link #toBuilder()} 或辅助方法创建新的 AgentContext。
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
         * Reason 阶段产生的结果。
         *
         * <p>
         * 初始 AgentContext 尚未执行 Reason，因此允许为 null。
         * </p>
         */
        private final ReasonResult reasonResult;

        /**
         * Planning 阶段产生的执行计划。
         *
         * <p>
         * 初始状态统一使用空计划，不使用 null。
         * </p>
         */
        private final AgentPlan agentPlan;

        /**
         * Agent 当前执行状态。
         */
        private final AgentStatus status;

        /**
         * Agent 执行日志。
         *
         * <p>
         * 这里只保存结构化执行摘要，不保存模型完整思维过程。
         * </p>
         */
        private final List<String> executionLogs;

        @Builder(toBuilder = true)
        private AgentContext(
                        String goal,
                        ReasonResult reasonResult,
                        AgentPlan agentPlan,
                        AgentStatus status,
                        List<String> executionLogs) {

                this.goal = normalizeGoal(goal);

                this.reasonResult = reasonResult;

                this.agentPlan = agentPlan == null
                                ? AgentPlan.empty()
                                : agentPlan;

                this.status = status == null
                                ? AgentStatus.CREATED
                                : status;

                this.executionLogs = normalizeExecutionLogs(
                                executionLogs);
        }

        /**
         * 根据 Goal 创建初始 AgentContext。
         *
         * <p>
         * 初始状态：
         * </p>
         *
         * <ul>
         * <li>status = CREATED</li>
         * <li>reasonResult = null</li>
         * <li>agentPlan = empty plan</li>
         * <li>executionLogs = empty list</li>
         * </ul>
         *
         * @param goal Agent 目标
         * @return 初始 AgentContext
         */
        public static AgentContext from(
                        String goal) {

                return AgentContext.builder()
                                .goal(goal)
                                .build();
        }

        /**
         * 判断 Agent 是否正在执行。
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
         * 判断当前上下文是否已经包含 Reason 结果。
         */
        public boolean hasReasonResult() {
                return reasonResult != null;
        }

        /**
         * 判断当前上下文是否已经包含有效执行计划。
         *
         * <p>
         * AgentPlan 永远不为 null，因此这里通过是否包含 Task 判断。
         * </p>
         */
        public boolean hasAgentPlan() {
                return agentPlan.hasTasks();
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
         * 创建包含 ReasonResult 的新 AgentContext。
         *
         * @param reasonResult Reason 阶段结果
         * @return 新 AgentContext
         */
        public AgentContext withReasonResult(
                        ReasonResult reasonResult) {

                Objects.requireNonNull(
                                reasonResult,
                                "ReasonResult must not be null");

                return toBuilder()
                                .reasonResult(reasonResult)
                                .build();
        }

        /**
         * 创建包含 AgentPlan 的新 AgentContext。
         *
         * <p>
         * 允许传入空计划，但不允许传入 null。
         * </p>
         *
         * @param agentPlan Planning 阶段产生的执行计划
         * @return 新 AgentContext
         */
        public AgentContext withAgentPlan(
                        AgentPlan agentPlan) {

                Objects.requireNonNull(
                                agentPlan,
                                "AgentPlan must not be null");

                return toBuilder()
                                .agentPlan(agentPlan)
                                .build();
        }

        /**
         * 创建一个追加日志后的新 AgentContext。
         *
         * @param executionLog 新执行日志
         * @return 追加日志后的新 AgentContext
         */
        public AgentContext appendExecutionLog(
                        String executionLog) {

                String normalizedLog = normalizeExecutionLog(
                                executionLog);

                List<String> updatedLogs = new ArrayList<>(
                                executionLogs);

                updatedLogs.add(
                                normalizedLog);

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
                                .map(
                                                AgentContext::normalizeExecutionLog)
                                .toList();

                return List.copyOf(
                                normalizedLogs);
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