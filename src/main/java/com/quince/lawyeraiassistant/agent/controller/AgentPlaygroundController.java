package com.quince.lawyeraiassistant.agent.controller;

import com.quince.lawyeraiassistant.agent.dto.AgentRequest;
import com.quince.lawyeraiassistant.agent.dto.AgentResponse;
import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.pipeline.AgentPipeline;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Agent Pipeline 开发诊断接口。
 *
 * <p>
 * 当前仅用于验证 AgentContext、AgentOperator 和
 * AgentPipeline 的基础执行流程。
 * </p>
 *
 * <p>
 * 当前仍使用 DummyReasonOperator 和 DummyPlanningOperator，
 * 不调用真实 LLM 或 Tool。
 * </p>
 */
@RestController
@RequestMapping("/api/playground/agent")
public class AgentPlaygroundController {

    private final AgentPipeline agentPipeline;

    public AgentPlaygroundController(
            AgentPipeline agentPipeline) {
        this.agentPipeline = Objects.requireNonNull(
                agentPipeline,
                "agentPipeline must not be null");
    }

    /**
     * 执行 Agent Pipeline。
     *
     * @param request Agent 请求
     * @return Agent 最终执行状态
     */
    @PostMapping
    public AgentResponse execute(
            @Valid @RequestBody AgentRequest request) {
        AgentContext initialContext = AgentContext.from(
                request.goal());

        AgentContext result = agentPipeline.execute(
                initialContext);

        return AgentResponse.from(result);
    }
}