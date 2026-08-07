package com.quince.lawyeraiassistant.agent.action;

import com.quince.lawyeraiassistant.agent.model.AgentActionDecision;
import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.ToolAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 基于 Spring AI 的 Agent Action Selector。
 *
 * <p>
 * 根据当前 AgentContext 和 AgentTask，
 * 调用 LLM 生成下一步 Action Decision，
 * 并转换为 Runtime ToolAction。
 * </p>
 */
@Component
public class SpringAiAgentActionSelector
        implements AgentActionSelector {

    private final ChatClient chatClient;

    private final AgentActionDecisionMapper decisionMapper;

    private final Resource actionSelectionPrompt;

    public SpringAiAgentActionSelector(
            ChatClient.Builder chatClientBuilder,
            AgentActionDecisionMapper decisionMapper,
            @Value("classpath:/prompts/agent/action-selection.st") Resource actionSelectionPrompt) {

        this.chatClient = Objects.requireNonNull(
                chatClientBuilder,
                "chatClientBuilder must not be null")
                .build();

        this.decisionMapper = Objects.requireNonNull(
                decisionMapper,
                "decisionMapper must not be null");

        this.actionSelectionPrompt = Objects.requireNonNull(
                actionSelectionPrompt,
                "actionSelectionPrompt must not be null");
    }

    @Override
    public ToolAction select(
            AgentContext context,
            AgentTask task) {

        Objects.requireNonNull(
                context,
                "AgentContext must not be null");

        Objects.requireNonNull(
                task,
                "AgentTask must not be null");

        AgentActionDecision decision = chatClient.prompt()
                .user(
                        userSpec -> userSpec
                                .text(
                                        actionSelectionPrompt)
                                .param(
                                        "goal",
                                        context.getGoal())
                                .param(
                                        "reasonSummary",
                                        resolveReasonSummary(
                                                context))
                                .param(
                                        "taskId",
                                        task.getId())
                                .param(
                                        "taskDescription",
                                        task.getDescription()))
                .call()
                .entity(
                        AgentActionDecision.class);

        return decisionMapper.map(
                task,
                decision);
    }

    private String resolveReasonSummary(
            AgentContext context) {

        if (!context.hasReasonResult()) {
            return "No reason result available.";
        }

        return context.getReasonResult()
                .getReasonSummary();
    }
}