package com.quince.lawyeraiassistant.agent.runtime;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.operator.ToolExecutionOperator;
import com.quince.lawyeraiassistant.agent.pipeline.AgentPipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 默认 Agent Runtime。
 *
 * <p>
 * 执行策略：
 * </p>
 *
 * <ol>
 * <li>首先执行一次完整 AgentPipeline</li>
 * <li>Pipeline 完成 Reason / Planning / 第一个 Tool Task</li>
 * <li>继续执行剩余 PENDING Task</li>
 * <li>所有 Task 完成后将 Agent 标记为 FINISHED</li>
 * <li>达到 maxSteps 时停止，防止无限循环</li>
 * </ol>
 */
@Component
public class DefaultAgentRuntime
        implements AgentRuntime {

    private final AgentPipeline agentPipeline;

    private final ToolExecutionOperator toolExecutionOperator;

    private final int maxSteps;

    public DefaultAgentRuntime(
            AgentPipeline agentPipeline,
            ToolExecutionOperator toolExecutionOperator,
            @Value("${agent.runtime.max-steps:10}") int maxSteps) {

        this.agentPipeline = Objects.requireNonNull(
                agentPipeline,
                "agentPipeline must not be null");

        this.toolExecutionOperator = Objects.requireNonNull(
                toolExecutionOperator,
                "toolExecutionOperator must not be null");

        if (maxSteps <= 0) {
            throw new IllegalArgumentException(
                    "maxSteps must be greater than zero");
        }

        this.maxSteps = maxSteps;
    }

    @Override
    public AgentContext run(
            AgentContext context) {

        Objects.requireNonNull(
                context,
                "AgentContext must not be null");

        /*
         * 第一轮：
         *
         * Reason
         * ↓
         * Planning
         * ↓
         * ToolExecution
         */
        AgentContext current = agentPipeline.execute(
                context);

        int executedSteps = current.observationCount();

        /*
         * 如果第一轮 Tool 已经失败，
         * 当前 Sprint 直接终止 Agent。
         *
         * Retry / Reflection 留到 Day14。
         */
        if (hasFailedTask(current)) {
            return markFailed(current);
        }

        /*
         * 后续轮次：
         *
         * 只继续执行 ToolExecutionOperator，
         * 不重新 Reason / Planning。
         */
        while (hasPendingTask(current)
                && executedSteps < maxSteps) {

            current = toolExecutionOperator.execute(
                    current);

            executedSteps++;

            if (hasFailedTask(current)) {
                return markFailed(current);
            }
        }

        /*
         * 所有 Task 已完成。
         */
        if (!hasPendingTask(current)) {
            return markFinished(current);
        }

        /*
         * 还有 Pending Task，
         * 但 maxSteps 已经耗尽。
         *
         * 当前保持 RUNNING。
         *
         * Day14 再引入更完整的
         * MAX_STEPS_EXCEEDED / Runtime Error Policy。
         */
        return current;
    }

    private boolean hasPendingTask(
            AgentContext context) {

        return context.getAgentPlan()
                .nextPendingTask()
                .isPresent();
    }

    private boolean hasFailedTask(
            AgentContext context) {

        return context.getAgentPlan()
                .getTasks()
                .stream()
                .anyMatch(
                        task -> task.getStatus() == com.quince.lawyeraiassistant.agent.model.AgentTaskStatus.FAILED);
    }

    private AgentContext markFinished(
            AgentContext context) {

        if (context.getStatus() == AgentStatus.FINISHED) {
            return context;
        }

        return context.toBuilder()
                .status(
                        AgentStatus.FINISHED)
                .build()
                .appendExecutionLog(
                        "Agent finished");
    }

    private AgentContext markFailed(
            AgentContext context) {

        if (context.getStatus() == AgentStatus.FAILED) {
            return context;
        }

        return context.toBuilder()
                .status(
                        AgentStatus.FAILED)
                .build()
                .appendExecutionLog(
                        "Agent failed");
    }
}