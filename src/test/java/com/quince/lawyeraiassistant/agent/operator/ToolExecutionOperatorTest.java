package com.quince.lawyeraiassistant.agent.operator;

import com.quince.lawyeraiassistant.agent.action.AgentActionSelector;
import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.AgentTaskStatus;
import com.quince.lawyeraiassistant.agent.model.ToolAction;
import com.quince.lawyeraiassistant.agent.model.ToolExecutionResult;
import com.quince.lawyeraiassistant.agent.model.ToolObservation;
import com.quince.lawyeraiassistant.agent.tool.AgentTool;
import com.quince.lawyeraiassistant.agent.tool.AgentToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolExecutionOperatorTest {

    private AgentActionSelector actionSelector;

    private AgentTool agentTool;

    private AgentToolRegistry toolRegistry;

    private ToolExecutionOperator operator;

    @BeforeEach
    void setUp() {

        actionSelector = mock(
                AgentActionSelector.class);

        agentTool = mock(
                AgentTool.class);

        when(
                agentTool.name()).thenReturn(
                        "searchLegalKnowledge");

        toolRegistry = new AgentToolRegistry(
                List.of(
                        agentTool));

        operator = new ToolExecutionOperator(
                actionSelector,
                toolRegistry);
    }

    @Test
    void shouldExecuteNextPendingTaskSuccessfully() {

        AgentContext context = createContext();

        AgentTask task = context.getAgentPlan()
                .nextPendingTask()
                .orElseThrow();

        ToolAction action = ToolAction.of(
                task.getId(),
                "searchLegalKnowledge",
                Map.of(
                        "legalQuestion",
                        "劳动合同违法解除的法律责任"));

        when(
                actionSelector.select(
                        context,
                        task))
                .thenReturn(
                        action);

        when(
                agentTool.execute(
                        action))
                .thenReturn(
                        ToolExecutionResult.success(
                                "劳动合同法第八十七条规定..."));

        AgentContext result = operator.execute(
                context);

        assertNotSame(
                context,
                result);

        AgentTask updatedTask = result.getAgentPlan()
                .findTaskById(
                        task.getId())
                .orElseThrow();

        assertEquals(
                AgentTaskStatus.COMPLETED,
                updatedTask.getStatus());

        assertEquals(
                1,
                result.observationCount());

        ToolObservation observation = result.getObservations()
                .getFirst();

        assertTrue(
                observation.isSuccess());

        assertEquals(
                task.getId(),
                observation.getTaskId());

        assertEquals(
                "searchLegalKnowledge",
                observation.getToolName());

        assertEquals(
                "劳动合同法第八十七条规定...",
                observation.getContent());

        assertTrue(
                result.getExecutionLogs()
                        .contains(
                                "Tool execution completed: "
                                        + task.getId()));

        /*
         * Immutable Context：
         * 原始 Context 仍然保持 PENDING。
         */
        AgentTask originalTask = context.getAgentPlan()
                .findTaskById(
                        task.getId())
                .orElseThrow();

        assertEquals(
                AgentTaskStatus.PENDING,
                originalTask.getStatus());

        assertFalse(
                context.hasObservations());

        verify(
                actionSelector).select(
                        context,
                        task);

        verify(
                agentTool).execute(
                        action);
    }

    @Test
    void shouldMarkTaskFailedWhenToolExecutionFails() {

        AgentContext context = createContext();

        AgentTask task = context.getAgentPlan()
                .nextPendingTask()
                .orElseThrow();

        ToolAction action = ToolAction.of(
                task.getId(),
                "searchLegalKnowledge",
                Map.of(
                        "legalQuestion",
                        "劳动合同解除"));

        when(
                actionSelector.select(
                        context,
                        task))
                .thenReturn(
                        action);

        when(
                agentTool.execute(
                        action))
                .thenReturn(
                        ToolExecutionResult.failure(
                                "VectorStore unavailable"));

        AgentContext result = operator.execute(
                context);

        AgentTask updatedTask = result.getAgentPlan()
                .findTaskById(
                        task.getId())
                .orElseThrow();

        assertEquals(
                AgentTaskStatus.FAILED,
                updatedTask.getStatus());

        assertEquals(
                1,
                result.observationCount());

        ToolObservation observation = result.getObservations()
                .getFirst();

        assertTrue(
                observation.isFailure());

        assertEquals(
                "VectorStore unavailable",
                observation.getErrorMessage());

        assertTrue(
                result.getExecutionLogs()
                        .contains(
                                "Tool execution failed: "
                                        + task.getId()));
    }

    @Test
    void shouldReturnSameContextWhenNoPendingTaskExists() {

        AgentTask completedTask = AgentTask.builder()
                .id("task-1")
                .description(
                        "查询法律规定")
                .status(
                        AgentTaskStatus.COMPLETED)
                .build();

        AgentContext context = AgentContext.builder()
                .goal(
                        "分析劳动合同")
                .agentPlan(
                        AgentPlan.from(
                                List.of(
                                        completedTask)))
                .build();

        AgentContext result = operator.execute(
                context);

        assertSame(
                context,
                result);

        verify(
                actionSelector,
                never()).select(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());

        verify(
                agentTool,
                never()).execute(
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectNullContext() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> operator.execute(
                        null));

        assertEquals(
                "AgentContext must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullActionSelector() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ToolExecutionOperator(
                        null,
                        toolRegistry));

        assertEquals(
                "actionSelector must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullToolRegistry() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ToolExecutionOperator(
                        actionSelector,
                        null));

        assertEquals(
                "toolRegistry must not be null",
                exception.getMessage());
    }

    private AgentContext createContext() {

        AgentTask task = AgentTask.builder()
                .id("task-1")
                .description(
                        "查询劳动合同违法解除相关法律规定")
                .status(
                        AgentTaskStatus.PENDING)
                .build();

        return AgentContext.builder()
                .goal(
                        "分析劳动合同")
                .agentPlan(
                        AgentPlan.from(
                                List.of(
                                        task)))
                .build();
    }
}