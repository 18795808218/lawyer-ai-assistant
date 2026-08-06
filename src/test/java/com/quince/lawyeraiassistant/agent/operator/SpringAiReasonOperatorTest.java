package com.quince.lawyeraiassistant.agent.operator;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.prompt.model.ReasonPromptContext;
import com.quince.lawyeraiassistant.agent.service.AgentReasonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

class SpringAiReasonOperatorTest {

    private AgentReasonService agentReasonService;

    private SpringAiReasonOperator operator;

    @BeforeEach
    void setUp() {
        agentReasonService = mock(AgentReasonService.class);

        operator = new SpringAiReasonOperator(
                agentReasonService);
    }

    @Test
    void shouldGenerateReasonResultAndUpdateContext() {
        AgentContext originalContext = AgentContext.from(
                "分析劳动合同并生成律师意见书");

        ReasonResult reasonResult = ReasonResult.from(
                "用户希望分析劳动合同并生成律师意见书。");

        when(
                agentReasonService.reason(
                        org.mockito.ArgumentMatchers
                                .any(ReasonPromptContext.class)))
                .thenReturn(reasonResult);

        AgentContext result = operator.execute(
                originalContext);

        assertNotSame(
                originalContext,
                result);

        assertEquals(
                AgentStatus.RUNNING,
                result.getStatus());

        assertTrue(
                result.hasReasonResult());

        assertSame(
                reasonResult,
                result.getReasonResult());

        assertEquals(
                "用户希望分析劳动合同并生成律师意见书。",
                result.getReasonResult()
                        .getReasonSummary());

        assertEquals(
                java.util.List.of(
                        "Reason completed"),
                result.getExecutionLogs());

        /*
         * 原始 Context 必须保持不变。
         */
        assertEquals(
                AgentStatus.CREATED,
                originalContext.getStatus());

        assertFalse(
                originalContext.hasReasonResult());

        assertTrue(
                originalContext
                        .getExecutionLogs()
                        .isEmpty());
    }

    @Test
    void shouldPassGoalToReasonService() {
        AgentContext context = AgentContext.from(
                "  分析竞业限制条款  ");

        ReasonResult reasonResult = ReasonResult.from(
                "用户希望分析竞业限制条款。");

        when(
                agentReasonService.reason(
                        org.mockito.ArgumentMatchers
                                .any(ReasonPromptContext.class)))
                .thenReturn(reasonResult);

        operator.execute(context);

        ArgumentCaptor<ReasonPromptContext> captor = ArgumentCaptor.forClass(
                ReasonPromptContext.class);

        verify(
                agentReasonService).reason(
                        captor.capture());

        assertEquals(
                "分析竞业限制条款",
                captor.getValue().getGoal());

        assertEquals(
                java.util.Map.of(
                        "goal",
                        "分析竞业限制条款"),
                captor.getValue()
                        .toVariables());
    }

    @Test
    void shouldPreserveExistingExecutionLogs() {
        AgentContext context = AgentContext.from(
                "分析劳动合同")
                .appendExecutionLog(
                        "Goal validated");

        ReasonResult reasonResult = ReasonResult.from(
                "用户希望分析劳动合同。");

        when(
                agentReasonService.reason(
                        org.mockito.ArgumentMatchers
                                .any(ReasonPromptContext.class)))
                .thenReturn(reasonResult);

        AgentContext result = operator.execute(context);

        assertEquals(
                java.util.List.of(
                        "Goal validated",
                        "Reason completed"),
                result.getExecutionLogs());
    }

    @Test
    void shouldReplaceExistingReasonResult() {
        ReasonResult oldReasonResult = ReasonResult.from(
                "旧推理结果");

        AgentContext context = AgentContext.from(
                "重新分析劳动合同")
                .withReasonResult(
                        oldReasonResult);

        ReasonResult newReasonResult = ReasonResult.from(
                "用户希望重新分析劳动合同。");

        when(
                agentReasonService.reason(
                        org.mockito.ArgumentMatchers
                                .any(ReasonPromptContext.class)))
                .thenReturn(newReasonResult);

        AgentContext result = operator.execute(context);

        assertSame(
                newReasonResult,
                result.getReasonResult());

        assertEquals(
                "用户希望重新分析劳动合同。",
                result.getReasonResult()
                        .getReasonSummary());
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
                agentReasonService,
                never()).reason(
                        org.mockito.ArgumentMatchers
                                .any(ReasonPromptContext.class));
    }

    @Test
    void shouldRejectNullReasonResultReturnedByService() {
        AgentContext context = AgentContext.from(
                "分析劳动合同");

        when(
                agentReasonService.reason(
                        org.mockito.ArgumentMatchers
                                .any(ReasonPromptContext.class)))
                .thenReturn(null);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> operator.execute(context));

        assertEquals(
                "AgentReasonService must not return null",
                exception.getMessage());
    }

    @Test
    void shouldPropagateReasonServiceException() {
        AgentContext context = AgentContext.from(
                "分析劳动合同");

        IllegalStateException expectedException = new IllegalStateException(
                "LLM reason failed");

        when(
                agentReasonService.reason(
                        org.mockito.ArgumentMatchers
                                .any(ReasonPromptContext.class)))
                .thenThrow(expectedException);

        IllegalStateException actualException = assertThrows(
                IllegalStateException.class,
                () -> operator.execute(context));

        assertSame(
                expectedException,
                actualException);
    }

    @Test
    void shouldRejectNullReasonService() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SpringAiReasonOperator(
                        null));

        assertEquals(
                "agentReasonService must not be null",
                exception.getMessage());
    }
}