package com.quince.lawyeraiassistant.agent.operator;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.prompt.model.PlanningPromptContext;
import com.quince.lawyeraiassistant.agent.service.AgentPlanningService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 基于 Spring AI 的 Planning Operator。
 *
 * <p>
 * 负责将 AgentContext 中的 Goal 和 ReasonResult
 * 转换为 PlanningPromptContext，
 * 调用 AgentPlanningService 获取 AgentPlan，
 * 并将结果写回新的 AgentContext。
 * </p>
 *
 * <pre>
 * AgentContext
 *      ↓
 * PlanningPromptContext
 *      ↓
 * AgentPlanningService
 *      ↓
 * AgentPlan
 *      ↓
 * AgentContext(RUNNING)
 * </pre>
 */
@Component
@Order(300)
public class SpringAiPlanningOperator
        implements AgentOperator {

    private static final String PLANNING_COMPLETED_LOG = "Planning completed";

    private final AgentPlanningService agentPlanningService;

    public SpringAiPlanningOperator(
            AgentPlanningService agentPlanningService) {

        this.agentPlanningService = Objects.requireNonNull(
                agentPlanningService,
                "agentPlanningService must not be null");
    }

    @Override
    public AgentContext execute(
            AgentContext context) {

        Objects.requireNonNull(
                context,
                "AgentContext must not be null");

        if (!context.hasReasonResult()) {
            throw new IllegalStateException(
                    "ReasonResult must exist before planning");
        }

        PlanningPromptContext planningPromptContext = PlanningPromptContext.from(
                context.getGoal(),
                context.getReasonResult());

        AgentPlan agentPlan = agentPlanningService.plan(
                planningPromptContext);

        Objects.requireNonNull(
                agentPlan,
                "AgentPlanningService must not return null");

        AgentContext plannedContext = context.toBuilder()
                .agentPlan(agentPlan)
                .status(AgentStatus.RUNNING)
                .build();

        return plannedContext.appendExecutionLog(
                PLANNING_COMPLETED_LOG);
    }
}