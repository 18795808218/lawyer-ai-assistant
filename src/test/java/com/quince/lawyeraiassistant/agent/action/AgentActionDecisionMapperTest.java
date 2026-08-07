package com.quince.lawyeraiassistant.agent.action;

import com.quince.lawyeraiassistant.agent.model.AgentActionDecision;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.AgentTaskStatus;
import com.quince.lawyeraiassistant.agent.model.ToolAction;
import com.quince.lawyeraiassistant.agent.tool.legal.LegalKnowledgeTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentActionDecisionMapperTest {

    private AgentActionDecisionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AgentActionDecisionMapper();
    }

    @Test
    void shouldMapDecisionToToolAction() {

        AgentTask task = createTask();

        AgentActionDecision decision = new AgentActionDecision(
                LegalKnowledgeTool.TOOL_NAME,
                Map.of(
                        "legalQuestion",
                        "劳动合同违法解除的法律责任"));

        ToolAction action = mapper.map(
                task,
                decision);

        assertEquals(
                "task-1",
                action.getTaskId());

        assertEquals(
                LegalKnowledgeTool.TOOL_NAME,
                action.getToolName());

        assertEquals(
                "劳动合同违法解除的法律责任",
                action.getArguments()
                        .get("legalQuestion"));
    }

    @Test
    void shouldNormalizeToolName() {

        AgentActionDecision decision = new AgentActionDecision(
                "  searchLegalKnowledge  ",
                Map.of());

        ToolAction action = mapper.map(
                createTask(),
                decision);

        assertEquals(
                LegalKnowledgeTool.TOOL_NAME,
                action.getToolName());
    }

    @Test
    void shouldNormalizeNullArgumentsToEmptyMap() {

        AgentActionDecision decision = new AgentActionDecision(
                LegalKnowledgeTool.TOOL_NAME,
                null);

        ToolAction action = mapper.map(
                createTask(),
                decision);

        assertTrue(
                action.getArguments()
                        .isEmpty());
    }

    @Test
    void shouldRejectNullTask() {

        AgentActionDecision decision = new AgentActionDecision(
                LegalKnowledgeTool.TOOL_NAME,
                Map.of());

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> mapper.map(
                        null,
                        decision));

        assertEquals(
                "AgentTask must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullDecision() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> mapper.map(
                        createTask(),
                        null));

        assertEquals(
                "Agent action decision must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullToolName() {

        AgentActionDecision decision = new AgentActionDecision(
                null,
                Map.of());

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> mapper.map(
                        createTask(),
                        decision));

        assertEquals(
                "Agent action decision toolName must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankToolName() {

        AgentActionDecision decision = new AgentActionDecision(
                "   ",
                Map.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.map(
                        createTask(),
                        decision));

        assertEquals(
                "Agent action decision toolName must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectUnsupportedTool() {

        AgentActionDecision decision = new AgentActionDecision(
                "inventedTool",
                Map.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.map(
                        createTask(),
                        decision));

        assertEquals(
                "Unsupported Agent tool: inventedTool",
                exception.getMessage());
    }

    private AgentTask createTask() {

        return AgentTask.builder()
                .id("task-1")
                .description(
                        "查询劳动合同违法解除相关法律规定")
                .status(
                        AgentTaskStatus.PENDING)
                .build();
    }
}