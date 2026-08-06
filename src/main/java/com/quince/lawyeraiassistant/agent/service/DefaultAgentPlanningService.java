package com.quince.lawyeraiassistant.agent.service;

import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.prompt.model.PlanningPromptContext;
import com.quince.lawyeraiassistant.prompt.builder.PromptBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AgentPlanningService 的默认实现。
 *
 * <p>
 * 使用 LLM 生成 Planning 文本，并解析为结构化 AgentPlan。
 * </p>
 */
@Service
public class DefaultAgentPlanningService
        implements AgentPlanningService {

    private static final String TASK_SEPARATOR = "\\|";

    private final ChatClient chatClient;

    private final PromptBuilder promptBuilder;

    public DefaultAgentPlanningService(
            @Qualifier("agentPlanningChatClient") ChatClient chatClient,
            PromptBuilder promptBuilder) {

        this.chatClient = Objects.requireNonNull(
                chatClient,
                "agentPlanningChatClient must not be null");

        this.promptBuilder = Objects.requireNonNull(
                promptBuilder,
                "PromptBuilder must not be null");
    }

    @Override
    public AgentPlan plan(
            PlanningPromptContext context) {

        Objects.requireNonNull(
                context,
                "PlanningPromptContext must not be null");

        Prompt prompt = promptBuilder.buildPlanning(
                context);

        String content = chatClient
                .prompt(prompt)
                .call()
                .content();

        if (content == null
                || content.isBlank()) {

            throw new IllegalStateException(
                    "Planning result must not be blank");
        }

        return parsePlan(content);
    }

    private AgentPlan parsePlan(
            String content) {

        String[] lines = content.strip()
                .split("\\R");

        List<AgentTask> tasks = new ArrayList<>();

        for (String line : lines) {
            String normalizedLine = line.trim();

            if (normalizedLine.isEmpty()) {
                continue;
            }

            tasks.add(
                    parseTask(normalizedLine));
        }

        if (tasks.isEmpty()) {
            throw new IllegalStateException(
                    "Planning result must contain at least one task");
        }

        return AgentPlan.from(tasks);
    }

    private AgentTask parseTask(
            String line) {

        String[] parts = line.split(
                TASK_SEPARATOR,
                2);

        if (parts.length != 2) {
            throw new IllegalStateException(
                    "Invalid planning task format: "
                            + line);
        }

        String taskId = parts[0].trim();

        String taskDescription = parts[1].trim();

        try {
            return AgentTask.pending(
                    taskId,
                    taskDescription);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "Invalid planning task: "
                            + line,
                    exception);
        }
    }
}