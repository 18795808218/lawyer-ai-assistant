package com.quince.lawyeraiassistant.agent.operator;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.prompt.model.ReasonPromptContext;
import com.quince.lawyeraiassistant.agent.service.AgentReasonService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 基于 Spring AI 的 Reason Operator。
 *
 * <p>
 * 负责将 AgentContext 中的 Goal 转换为 ReasonPromptContext，
 * 调用 AgentReasonService 获取 ReasonResult，
 * 并将结果写回新的 AgentContext。
 * </p>
 *
 * <p>
 * 执行链：
 * </p>
 *
 * <pre>
 * AgentContext
 *      ↓
 * ReasonPromptContext
 *      ↓
 * AgentReasonService
 *      ↓
 * ReasonResult
 *      ↓
 * AgentContext(RUNNING)
 * </pre>
 */
@Component
@Order(200)
public class SpringAiReasonOperator
        implements AgentOperator {

    private static final String REASON_COMPLETED_LOG = "Reason completed";

    private final AgentReasonService agentReasonService;

    public SpringAiReasonOperator(
            AgentReasonService agentReasonService) {

        this.agentReasonService = Objects.requireNonNull(
                agentReasonService,
                "agentReasonService must not be null");
    }

    @Override
    public AgentContext execute(
            AgentContext context) {

        Objects.requireNonNull(
                context,
                "AgentContext must not be null");

        ReasonPromptContext reasonPromptContext = ReasonPromptContext.from(
                context.getGoal());

        ReasonResult reasonResult = agentReasonService.reason(
                reasonPromptContext);

        Objects.requireNonNull(
                reasonResult,
                "AgentReasonService must not return null");

        AgentContext reasonedContext = context.toBuilder()
                .reasonResult(reasonResult)
                .status(AgentStatus.RUNNING)
                .build();

        return reasonedContext.appendExecutionLog(
                REASON_COMPLETED_LOG);
    }
}