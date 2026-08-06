package com.quince.lawyeraiassistant.agent.dto;

import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.AgentTaskStatus;

import java.util.Objects;

/**
 * Agent Task 响应。
 *
 * @param id          任务 ID
 * @param description 任务描述
 * @param status      任务状态
 */
public record AgentTaskResponse(

        String id,

        String description,

        AgentTaskStatus status

) {

    public AgentTaskResponse {
        Objects.requireNonNull(
                id,
                "id must not be null");

        Objects.requireNonNull(
                description,
                "description must not be null");

        Objects.requireNonNull(
                status,
                "status must not be null");
    }

    public static AgentTaskResponse from(
            AgentTask task) {

        Objects.requireNonNull(
                task,
                "AgentTask must not be null");

        return new AgentTaskResponse(
                task.getId(),
                task.getDescription(),
                task.getStatus());
    }
}