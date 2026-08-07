package com.quince.lawyeraiassistant.agent.action;

import com.quince.lawyeraiassistant.agent.model.AgentActionDecision;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.ToolAction;
import com.quince.lawyeraiassistant.agent.tool.legal.LegalKnowledgeTool;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 将 LLM Action Decision 转换为 Runtime ToolAction。
 */
@Component
public class AgentActionDecisionMapper {

    public ToolAction map(
            AgentTask task,
            AgentActionDecision decision) {

        Objects.requireNonNull(
                task,
                "AgentTask must not be null");

        Objects.requireNonNull(
                decision,
                "Agent action decision must not be null");

        String toolName = normalizeToolName(
                decision.toolName());

        validateSupportedTool(
                toolName);

        Map<String, Object> arguments = decision.arguments() == null
                ? Map.of()
                : decision.arguments();

        return ToolAction.of(
                task.getId(),
                toolName,
                arguments);
    }

    private String normalizeToolName(
            String toolName) {

        Objects.requireNonNull(
                toolName,
                "Agent action decision toolName must not be null");

        String normalized = toolName.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "Agent action decision toolName must not be blank");
        }

        return normalized;
    }

    private void validateSupportedTool(
            String toolName) {

        if (!LegalKnowledgeTool.TOOL_NAME.equals(
                toolName)) {

            throw new IllegalArgumentException(
                    "Unsupported Agent tool: "
                            + toolName);
        }
    }
}