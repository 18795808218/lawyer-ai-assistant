package com.quince.lawyeraiassistant.agent.operator;

import com.quince.lawyeraiassistant.agent.model.AgentContext;

/**
 * Agent Pipeline 节点接口。
 *
 * <p>
 * 每个 AgentOperator 负责 Agent 生命周期中的一个阶段，
 * 例如：
 * </p>
 *
 * <ul>
 * <li>Reason</li>
 * <li>Planning</li>
 * <li>Tool Calling</li>
 * <li>Reflection</li>
 * </ul>
 *
 * <p>
 * 实现类必须返回新的 {@link AgentContext}，
 * 不允许直接修改传入对象。
 * </p>
 */
public interface AgentOperator {

    /**
     * 执行当前 Agent 节点。
     *
     * @param context 当前 AgentContext
     * @return 更新后的 AgentContext
     */
    AgentContext execute(
            AgentContext context);

}