package com.quince.lawyeraiassistant.agent.pipeline;

import com.quince.lawyeraiassistant.agent.model.AgentContext;

/**
 * Agent Pipeline 的统一执行入口。
 *
 * <p>
 * 负责将 AgentContext 依次交给多个 AgentOperator，
 * 并返回全部节点执行完成后的最终上下文。
 * </p>
 */
@FunctionalInterface
public interface AgentPipeline {

    /**
     * 执行 Agent Pipeline。
     *
     * @param context 初始 AgentContext
     * @return 最终 AgentContext
     */
    AgentContext execute(
            AgentContext context);
}