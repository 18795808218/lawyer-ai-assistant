package com.quince.lawyeraiassistant.agent.operator;

import com.quince.lawyeraiassistant.agent.action.AgentActionSelector;
import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.AgentTaskStatus;
import com.quince.lawyeraiassistant.agent.model.ToolAction;
import com.quince.lawyeraiassistant.agent.model.ToolExecutionResult;
import com.quince.lawyeraiassistant.agent.model.ToolObservation;
import com.quince.lawyeraiassistant.agent.tool.AgentTool;
import com.quince.lawyeraiassistant.agent.tool.AgentToolRegistry;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Agent Tool Execution Operator。
 *
 * <p>
 * 负责执行 AgentPlan 中的下一个 PENDING Task。
 * </p>
 *
 * <pre>
 * AgentContext
 *      ↓
 * nextPendingTask
 *      ↓
 * AgentActionSelector
 *      ↓
 * ToolAction
 *      ↓
 * AgentToolRegistry
 *      ↓
 * AgentTool
 *      ↓
 * ToolExecutionResult
 *      ↓
 * ToolObservation
 *      ↓
 * AgentContext
 * </pre>
 */
@Component
@Order(400)
public class ToolExecutionOperator
        implements AgentOperator {

    private final AgentActionSelector actionSelector;

    private final AgentToolRegistry toolRegistry;

    public ToolExecutionOperator(
            AgentActionSelector actionSelector,
            AgentToolRegistry toolRegistry) {

        this.actionSelector = Objects.requireNonNull(
                actionSelector,
                "actionSelector must not be null");

        this.toolRegistry = Objects.requireNonNull(
                toolRegistry,
                "toolRegistry must not be null");
    }

    @Override
    public AgentContext execute(
            AgentContext context) {

        Objects.requireNonNull(
                context,
                "AgentContext must not be null");

        AgentPlan currentPlan = context.getAgentPlan();

        AgentTask task = currentPlan.nextPendingTask()
                .orElse(null);

        if (task == null) {
            return context;
        }

        /*
         * Action Selection
         */
        ToolAction action = actionSelector.select(
                context,
                task);

        /*
         * 根据 ToolAction 找到真正的 AgentTool。
         */
        AgentTool tool = toolRegistry.get(
                action.getToolName());

        /*
         * Task:
         *
         * PENDING → RUNNING
         */
        AgentPlan runningPlan = currentPlan.updateTaskStatus(
                task.getId(),
                AgentTaskStatus.RUNNING);

        AgentContext runningContext = context.toBuilder()
                .agentPlan(runningPlan)
                .build();

        /*
         * 真正执行 Tool。
         */
        ToolExecutionResult result = tool.execute(
                action);

        if (result.isSuccess()) {
            return handleSuccess(
                    runningContext,
                    task,
                    action,
                    result);
        }

        return handleFailure(
                runningContext,
                task,
                action,
                result);
    }

    private AgentContext handleSuccess(
            AgentContext context,
            AgentTask task,
            ToolAction action,
            ToolExecutionResult result) {

        AgentPlan completedPlan = context.getAgentPlan()
                .updateTaskStatus(
                        task.getId(),
                        AgentTaskStatus.COMPLETED);

        ToolObservation observation = ToolObservation.success(
                task.getId(),
                action.getToolName(),
                result.getContent());

        return context.toBuilder()
                .agentPlan(
                        completedPlan)
                .build()
                .appendObservation(
                        observation)
                .appendExecutionLog(
                        "Tool execution completed: "
                                + task.getId());
    }

    private AgentContext handleFailure(
            AgentContext context,
            AgentTask task,
            ToolAction action,
            ToolExecutionResult result) {

        AgentPlan failedPlan = context.getAgentPlan()
                .updateTaskStatus(
                        task.getId(),
                        AgentTaskStatus.FAILED);

        ToolObservation observation = ToolObservation.failure(
                task.getId(),
                action.getToolName(),
                result.getErrorMessage());

        return context.toBuilder()
                .agentPlan(
                        failedPlan)
                .build()
                .appendObservation(
                        observation)
                .appendExecutionLog(
                        "Tool execution failed: "
                                + task.getId());
    }
}