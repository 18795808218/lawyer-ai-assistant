package com.quince.lawyeraiassistant.agent.dto;

import com.quince.lawyeraiassistant.agent.model.ToolObservation;

import java.util.Objects;

/**
 * Agent Tool Observation 响应。
 *
 * <p>
 * 用于向 API 调用方暴露 Agent Runtime
 * 在 Tool 执行阶段观察到的结构化结果。
 * </p>
 *
 * @param taskId       对应 AgentTask ID
 * @param toolName     Tool 名称
 * @param success      Tool 是否执行成功
 * @param content      Tool 成功执行后的结果
 * @param errorMessage Tool 执行失败时的错误信息
 */
public record ToolObservationResponse(

        String taskId,

        String toolName,

        boolean success,

        String content,

        String errorMessage

) {

    /**
     * 从 Domain ToolObservation 创建 API Response。
     *
     * @param observation Tool Observation
     * @return ToolObservationResponse
     */
    public static ToolObservationResponse from(
            ToolObservation observation) {

        Objects.requireNonNull(
                observation,
                "ToolObservation must not be null");

        return new ToolObservationResponse(
                observation.getTaskId(),
                observation.getToolName(),
                observation.isSuccess(),
                observation.getContent(),
                observation.getErrorMessage());
    }
}