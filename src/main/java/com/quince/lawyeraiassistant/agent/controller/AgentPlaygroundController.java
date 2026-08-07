package com.quince.lawyeraiassistant.agent.controller;

import com.quince.lawyeraiassistant.agent.dto.AgentRequest;
import com.quince.lawyeraiassistant.agent.dto.AgentResponse;
import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.runtime.AgentRuntime;
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
 * 用于验证 Agent Runtime 的完整执行流程，
 * 包括 Reason、Planning、Action Selection、
 * Tool Execution、Observation 和 Multi-step Execution。
 * </p>
 *
 * <p>
 * 当前接口属于 Playground / Development API，
 * 用于 Agent Runtime 开发和联调，
 * 不作为最终业务 API。
 * </p>
 */
@RestController
@RequestMapping("/api/playground/agent")
public class AgentPlaygroundController {

        private final AgentRuntime agentRuntime;

        public AgentPlaygroundController(
                        AgentRuntime agentRuntime) {

                this.agentRuntime = Objects.requireNonNull(
                                agentRuntime,
                                "agentRuntime must not be null");
        }

        /**
         * 执行一次完整 Agent Runtime。
         *
         * @param request Agent 请求
         * @return Agent 执行结果
         */
        @PostMapping
        public AgentResponse run(
                        @Valid @RequestBody AgentRequest request) {

                AgentContext initialContext = AgentContext.from(
                                request.goal());

                AgentContext result = agentRuntime.run(
                                initialContext);

                return AgentResponse.from(
                                result);
        }
}