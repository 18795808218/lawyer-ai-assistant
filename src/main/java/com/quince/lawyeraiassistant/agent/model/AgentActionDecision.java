package com.quince.lawyeraiassistant.agent.model;

import java.util.Map;

/**
 * LLM 对下一步 Tool Action 的决策结果。
 *
 * <p>
 * 本对象属于 Action Decision 阶段的 Structured Output Model。
 * </p>
 *
 * <p>
 * 它不是最终 Runtime ToolAction。
 * Runtime 仍然需要对该结果进行校验和转换。
 * </p>
 *
 * @param toolName  Tool 名称
 * @param arguments Tool 参数
 */
public record AgentActionDecision(
        String toolName,
        Map<String, Object> arguments) {
}