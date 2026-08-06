package com.quince.lawyeraiassistant.agent.service;

import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.prompt.model.PlanningPromptContext;

/**
 * Agent Planning 服务。
 */
public interface AgentPlanningService {

    /**
     * 根据 PlanningPromptContext 生成 AgentPlan。
     *
     * @param context Planning Prompt 上下文
     * @return 结构化 AgentPlan
     */
    AgentPlan plan(
            PlanningPromptContext context);
}