package com.quince.lawyeraiassistant.agent.service;

import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.prompt.model.ReasonPromptContext;

/**
 * Agent Reason 服务。
 *
 * <p>
 * 负责调用 LLM，
 * 根据 ReasonPromptContext 生成 ReasonResult。
 * </p>
 */
public interface AgentReasonService {

    /**
     * 执行 Reason。
     *
     * @param context Reason Prompt 上下文
     * @return ReasonResult
     */
    ReasonResult reason(
            ReasonPromptContext context);

}