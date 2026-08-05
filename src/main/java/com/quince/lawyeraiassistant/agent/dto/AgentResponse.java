package com.quince.lawyeraiassistant.agent.dto;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;

import java.util.List;
import java.util.Objects;

/**
 * Agent Playground 响应。
 *
 * @param goal          Agent 目标
 * @param status        Agent 当前状态
 * @param executionLogs Agent 执行日志
 */
public record AgentResponse(

        String goal,

        AgentStatus status,

        List<String> executionLogs

) {

    public AgentResponse {
        Objects.requireNonNull(
                goal,
                "goal must not be null");

        Objects.requireNonNull(
                status,
                "status must not be null");

        executionLogs = executionLogs == null
                ? List.of()
                : List.copyOf(executionLogs);
    }

    /**
     * 根据 AgentContext 创建响应。
     */
    public static AgentResponse from(
            AgentContext context) {
        Objects.requireNonNull(
                context,
                "AgentContext must not be null");

        return new AgentResponse(
                context.getGoal(),
                context.getStatus(),
                context.getExecutionLogs());
    }
}