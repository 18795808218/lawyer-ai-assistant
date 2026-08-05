package com.quince.lawyeraiassistant.agent.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Agent Playground 请求。
 *
 * @param goal Agent 需要完成的目标
 */
public record AgentRequest(

        @NotBlank(message = "goal must not be blank") 
        String goal

) {
}