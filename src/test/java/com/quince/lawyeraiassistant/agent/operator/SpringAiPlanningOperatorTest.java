package com.quince.lawyeraiassistant.agent.operator;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.prompt.model.PlanningPromptContext;
import com.quince.lawyeraiassistant.agent.service.AgentPlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SpringAiPlanningOperatorTest {

    private AgentPlanningService planningService;

    private SpringAiPlanningOperator operator;

    @BeforeEach
    void setUp() {

        planningService = mock(AgentPlanningService.class);

        operator = new SpringAiPlanningOperator(
                planningService);
    }

    @Test
    void shouldGenerateAgentPlanAndUpdateContext() {

        AgentContext originalContext = AgentContext.from(
                "分析劳动合同")
                .withReasonResult(
                        ReasonResult.from(
                                "用户希望分析劳动合同。"));

        AgentPlan plan = AgentPlan.from(
                List.of(
                        AgentTask.pending(
                                "task-1",
                                "读取劳动合同")));

        when(
                planningService.plan(
                        any(PlanningPromptContext.class)))
                .thenReturn(plan);

        AgentContext result = operator.execute(
                originalContext);

        assertNotSame(
                originalContext,
                result);

        assertTrue(
                result.hasAgentPlan());

        assertSame(
                plan,
                result.getAgentPlan());

        assertEquals(
                AgentStatus.RUNNING,
                result.getStatus());

        assertEquals(
                List.of(
                        "Planning completed"),
                result.getExecutionLogs());

        /*
         * 原 Context 不变
         */
        assertFalse(
                originalContext.hasAgentPlan());

        assertTrue(
                originalContext
                        .getExecutionLogs()
                        .isEmpty());
    }

    @Test
    void shouldPassGoalAndReasonResultToPlanningService() {

        ReasonResult reason = ReasonResult.from(
                "分析劳动合同。");

        AgentContext context = AgentContext.from(
                "分析劳动合同")
                .withReasonResult(
                        reason);

        when(
                planningService.plan(
                        any()))
                .thenReturn(
                        AgentPlan.empty());

        operator.execute(
                context);

        ArgumentCaptor<PlanningPromptContext> captor = ArgumentCaptor.forClass(
                PlanningPromptContext.class);

        verify(
                planningService).plan(
                        captor.capture());

        PlanningPromptContext promptContext = captor.getValue();

        assertEquals(
                "分析劳动合同",
                promptContext.getGoal());

        assertSame(
                reason,
                promptContext.getReasonResult());
    }

    @Test
    void shouldPreserveReasonResultAfterPlanning() {

        ReasonResult reason = ReasonResult.from(
                "分析劳动合同。");

        AgentPlan plan = AgentPlan.from(
                List.of(
                        AgentTask.pending(
                                "task-1",
                                "读取劳动合同")));

        AgentContext context = AgentContext.from(
                "分析劳动合同")
                .withReasonResult(
                        reason);

        when(
                planningService.plan(
                        any()))
                .thenReturn(
                        plan);

        AgentContext result = operator.execute(
                context);

        assertSame(
                reason,
                result.getReasonResult());

        assertSame(
                plan,
                result.getAgentPlan());
    }

    @Test
    void shouldAppendPlanningCompletedLog() {

        AgentContext context = AgentContext.from(
                "分析劳动合同")
                .withReasonResult(
                        ReasonResult.from(
                                "分析劳动合同。"))
                .appendExecutionLog(
                        "Reason completed");

        when(
                planningService.plan(
                        any()))
                .thenReturn(
                        AgentPlan.empty());

        AgentContext result = operator.execute(
                context);

        assertEquals(
                List.of(
                        "Reason completed",
                        "Planning completed"),
                result.getExecutionLogs());
    }

    @Test
    void shouldRejectNullContext() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> operator.execute(null));

        assertEquals(
                "AgentContext must not be null",
                exception.getMessage());

        verify(
                planningService,
                never()).plan(
                        any());
    }

    @Test
    void shouldRejectContextWithoutReasonResult() {

        AgentContext context = AgentContext.from(
                "分析劳动合同");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> operator.execute(
                        context));

        assertEquals(
                "ReasonResult must exist before planning",
                exception.getMessage());

        verify(
                planningService,
                never()).plan(
                        any());
    }

    @Test
    void shouldRejectNullPlanReturnedByService() {

        AgentContext context = AgentContext.from(
                "分析劳动合同")
                .withReasonResult(
                        ReasonResult.from(
                                "分析劳动合同。"));

        when(
                planningService.plan(
                        any()))
                .thenReturn(
                        null);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> operator.execute(
                        context));

        assertEquals(
                "AgentPlanningService must not return null",
                exception.getMessage());
    }

    @Test
    void shouldPropagatePlanningException() {

        AgentContext context = AgentContext.from(
                "分析劳动合同")
                .withReasonResult(
                        ReasonResult.from(
                                "分析劳动合同。"));

        IllegalStateException expected = new IllegalStateException(
                "Planning failed");

        when(
                planningService.plan(
                        any()))
                .thenThrow(
                        expected);

        IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> operator.execute(
                        context));

        assertSame(
                expected,
                actual);
    }

    @Test
    void shouldRejectNullPlanningService() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SpringAiPlanningOperator(
                        null));

        assertEquals(
                "agentPlanningService must not be null",
                exception.getMessage());
    }

}