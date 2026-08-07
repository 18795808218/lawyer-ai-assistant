package com.quince.lawyeraiassistant.agent.runtime;

import com.quince.lawyeraiassistant.agent.action.AgentActionSelector;
import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.AgentTaskStatus;
import com.quince.lawyeraiassistant.agent.model.ToolAction;
import com.quince.lawyeraiassistant.agent.model.ToolExecutionResult;
import com.quince.lawyeraiassistant.agent.operator.ToolExecutionOperator;
import com.quince.lawyeraiassistant.agent.pipeline.AgentPipeline;
import com.quince.lawyeraiassistant.agent.tool.AgentTool;
import com.quince.lawyeraiassistant.agent.tool.AgentToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultAgentRuntimeTest {

    private AgentPipeline agentPipeline;

    private AgentActionSelector actionSelector;

    private AgentTool agentTool;

    private ToolExecutionOperator toolExecutionOperator;

    @BeforeEach
    void setUp() {

        agentPipeline = mock(
                AgentPipeline.class);

        actionSelector = mock(
                AgentActionSelector.class);

        agentTool = mock(
                AgentTool.class);

        when(
                agentTool.name())
                .thenReturn(
                        "searchLegalKnowledge");

        AgentToolRegistry toolRegistry = new AgentToolRegistry(
                List.of(
                        agentTool));

        toolExecutionOperator = new ToolExecutionOperator(
                actionSelector,
                toolRegistry);
    }

    @Test
    void shouldExecuteRemainingPendingTasksAndFinish() {

        AgentTask firstTask = AgentTask.pending(
                "task-1",
                "查询法律条款")
                .withStatus(
                        AgentTaskStatus.COMPLETED);

        AgentTask secondTask = AgentTask.pending(
                "task-2",
                "查询违法解除责任");

        AgentTask thirdTask = AgentTask.pending(
                "task-3",
                "查询赔偿标准");

        AgentContext afterPipeline = AgentContext.builder()
                .goal(
                        "查询违法解除劳动合同的法律责任")
                .agentPlan(
                        AgentPlan.from(
                                List.of(
                                        firstTask,
                                        secondTask,
                                        thirdTask)))
                .status(
                        com.quince.lawyeraiassistant.agent.model.AgentStatus.RUNNING)
                .build();

        when(
                agentPipeline.execute(
                        any(
                                AgentContext.class)))
                .thenReturn(
                        afterPipeline);

        when(
                actionSelector.select(
                        any(
                                AgentContext.class),
                        any(
                                AgentTask.class)))
                .thenAnswer(
                        invocation -> {

                            AgentTask task = invocation.getArgument(1);

                            return ToolAction.of(
                                    task.getId(),
                                    "searchLegalKnowledge",
                                    Map.of(
                                            "legalQuestion",
                                            task.getDescription()));
                        });

        when(
                agentTool.execute(
                        any(
                                ToolAction.class)))
                .thenReturn(
                        ToolExecutionResult.success(
                                "法律知识检索结果"));

        DefaultAgentRuntime runtime = new DefaultAgentRuntime(
                agentPipeline,
                toolExecutionOperator,
                10);

        AgentContext result = runtime.run(
                AgentContext.from(
                        "查询违法解除劳动合同的法律责任"));

        assertEquals(
                AgentTaskStatus.COMPLETED,
                result.getAgentPlan()
                        .findTaskById(
                                "task-1")
                        .orElseThrow()
                        .getStatus());

        assertEquals(
                AgentTaskStatus.COMPLETED,
                result.getAgentPlan()
                        .findTaskById(
                                "task-2")
                        .orElseThrow()
                        .getStatus());

        assertEquals(
                AgentTaskStatus.COMPLETED,
                result.getAgentPlan()
                        .findTaskById(
                                "task-3")
                        .orElseThrow()
                        .getStatus());

        assertEquals(
                com.quince.lawyeraiassistant.agent.model.AgentStatus.FINISHED,
                result.getStatus());

        assertEquals(
                2,
                result.observationCount());

        assertEquals(
                "Agent finished",
                result.getExecutionLogs()
                        .getLast());
    }

    @Test
    void shouldStopWhenMaxStepsIsReached() {

        AgentTask firstTask = AgentTask.pending(
                "task-1",
                "查询法律条款")
                .withStatus(
                        AgentTaskStatus.COMPLETED);

        AgentTask secondTask = AgentTask.pending(
                "task-2",
                "查询法律责任");

        AgentTask thirdTask = AgentTask.pending(
                "task-3",
                "查询赔偿标准");

        AgentContext afterPipeline = AgentContext.builder()
                .goal(
                        "分析劳动合同")
                .agentPlan(
                        AgentPlan.from(
                                List.of(
                                        firstTask,
                                        secondTask,
                                        thirdTask)))
                .status(
                        com.quince.lawyeraiassistant.agent.model.AgentStatus.RUNNING)
                .build();

        when(
                agentPipeline.execute(
                        any(
                                AgentContext.class)))
                .thenReturn(
                        afterPipeline);

        when(
                actionSelector.select(
                        any(
                                AgentContext.class),
                        any(
                                AgentTask.class)))
                .thenAnswer(
                        invocation -> {

                            AgentTask task = invocation.getArgument(1);

                            return ToolAction.of(
                                    task.getId(),
                                    "searchLegalKnowledge",
                                    Map.of(
                                            "legalQuestion",
                                            task.getDescription()));
                        });

        when(
                agentTool.execute(
                        any(
                                ToolAction.class)))
                .thenReturn(
                        ToolExecutionResult.success(
                                "检索结果"));

        DefaultAgentRuntime runtime = new DefaultAgentRuntime(
                agentPipeline,
                toolExecutionOperator,
                1);

        AgentContext result = runtime.run(
                AgentContext.from(
                        "分析劳动合同"));

        /*
         * Pipeline 返回时 task-1 已完成，
         * observationCount 当前测试数据是 0。
         *
         * Runtime 还有一个 step budget，
         * 因此 task-2 被执行。
         */
        assertEquals(
                AgentTaskStatus.COMPLETED,
                result.getAgentPlan()
                        .findTaskById(
                                "task-2")
                        .orElseThrow()
                        .getStatus());

        assertEquals(
                AgentTaskStatus.PENDING,
                result.getAgentPlan()
                        .findTaskById(
                                "task-3")
                        .orElseThrow()
                        .getStatus());

        assertEquals(
                com.quince.lawyeraiassistant.agent.model.AgentStatus.RUNNING,
                result.getStatus());
    }

    @Test
    void shouldMarkAgentFailedWhenToolFails() {

        AgentTask firstTask = AgentTask.pending(
                "task-1",
                "查询法律条款")
                .withStatus(
                        AgentTaskStatus.COMPLETED);

        AgentTask secondTask = AgentTask.pending(
                "task-2",
                "查询法律责任");

        AgentContext afterPipeline = AgentContext.builder()
                .goal(
                        "分析劳动合同")
                .agentPlan(
                        AgentPlan.from(
                                List.of(
                                        firstTask,
                                        secondTask)))
                .status(
                        com.quince.lawyeraiassistant.agent.model.AgentStatus.RUNNING)
                .build();

        when(
                agentPipeline.execute(
                        any(
                                AgentContext.class)))
                .thenReturn(
                        afterPipeline);

        when(
                actionSelector.select(
                        any(
                                AgentContext.class),
                        any(
                                AgentTask.class)))
                .thenReturn(
                        ToolAction.of(
                                "task-2",
                                "searchLegalKnowledge",
                                Map.of(
                                        "legalQuestion",
                                        "违法解除劳动合同")));

        when(
                agentTool.execute(
                        any(
                                ToolAction.class)))
                .thenReturn(
                        ToolExecutionResult.failure(
                                "VectorStore unavailable"));

        DefaultAgentRuntime runtime = new DefaultAgentRuntime(
                agentPipeline,
                toolExecutionOperator,
                10);

        AgentContext result = runtime.run(
                AgentContext.from(
                        "分析劳动合同"));

        assertEquals(
                AgentTaskStatus.FAILED,
                result.getAgentPlan()
                        .findTaskById(
                                "task-2")
                        .orElseThrow()
                        .getStatus());

        assertEquals(
                com.quince.lawyeraiassistant.agent.model.AgentStatus.FAILED,
                result.getStatus());

        assertEquals(
                "Agent failed",
                result.getExecutionLogs()
                        .getLast());
    }

    @Test
    void shouldRejectInvalidMaxSteps() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new DefaultAgentRuntime(
                        agentPipeline,
                        toolExecutionOperator,
                        0));

        assertEquals(
                "maxSteps must be greater than zero",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullContext() {

        DefaultAgentRuntime runtime = new DefaultAgentRuntime(
                agentPipeline,
                toolExecutionOperator,
                10);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> runtime.run(
                        null));

        assertEquals(
                "AgentContext must not be null",
                exception.getMessage());
    }
}