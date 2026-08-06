package com.quince.lawyeraiassistant.agent.pipeline;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.operator.SpringAiPlanningOperator;
import com.quince.lawyeraiassistant.agent.operator.SpringAiReasonOperator;
import com.quince.lawyeraiassistant.agent.prompt.model.PlanningPromptContext;
import com.quince.lawyeraiassistant.agent.prompt.model.ReasonPromptContext;
import com.quince.lawyeraiassistant.agent.service.AgentPlanningService;
import com.quince.lawyeraiassistant.agent.service.AgentReasonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Agent Pipeline 完整流程测试。
 *
 * <p>
 * 不启动 Spring 容器，也不调用真实 LLM。
 * 使用真实 Reason/Planning Operator，
 * 并 Mock 对应 Service，验证 Agent Pipeline 的完整数据流。
 * </p>
 *
 * <pre>
 * AgentContext(CREATED)
 *          ↓
 * SpringAiReasonOperator
 *          ↓
 * ReasonResult
 *          ↓
 * SpringAiPlanningOperator
 *          ↓
 * AgentPlan
 *          ↓
 * AgentContext(RUNNING)
 * </pre>
 */
class AgentPipelineFlowTest {

        private AgentReasonService agentReasonService;

        private AgentPlanningService agentPlanningService;

        private SpringAiReasonOperator reasonOperator;

        private SpringAiPlanningOperator planningOperator;

        @BeforeEach
        void setUp() {
                agentReasonService = mock(AgentReasonService.class);

                agentPlanningService = mock(AgentPlanningService.class);

                reasonOperator = new SpringAiReasonOperator(
                                agentReasonService);

                planningOperator = new SpringAiPlanningOperator(
                                agentPlanningService);
        }

        @Test
        void shouldExecuteReasonThenPlanning() {
                ReasonResult reasonResult = ReasonResult.from(
                                "用户希望分析劳动合同并生成律师意见书。");

                AgentPlan agentPlan = AgentPlan.from(
                                List.of(
                                                AgentTask.pending(
                                                                "task-1",
                                                                "读取劳动合同"),
                                                AgentTask.pending(
                                                                "task-2",
                                                                "识别法律风险"),
                                                AgentTask.pending(
                                                                "task-3",
                                                                "生成律师意见书")));

                when(
                                agentReasonService.reason(
                                                any(ReasonPromptContext.class)))
                                .thenReturn(reasonResult);

                when(
                                agentPlanningService.plan(
                                                any(PlanningPromptContext.class)))
                                .thenReturn(agentPlan);

                /*
                 * 故意倒序传入，验证 @Order 自动排序。
                 */
                AgentPipeline pipeline = new DefaultAgentPipeline(
                                List.of(
                                                planningOperator,
                                                reasonOperator));

                AgentContext originalContext = AgentContext.from(
                                "分析劳动合同并生成律师意见书");

                AgentContext result = pipeline.execute(
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

                assertTrue(
                                result.hasAgentPlan());

                assertSame(
                                agentPlan,
                                result.getAgentPlan());

                assertEquals(
                                3,
                                result.getAgentPlan()
                                                .taskCount());

                assertEquals(
                                List.of(
                                                "Reason completed",
                                                "Planning completed"),
                                result.getExecutionLogs());

                /*
                 * 原始 Context 保持不变。
                 */
                assertEquals(
                                AgentStatus.CREATED,
                                originalContext.getStatus());

                assertFalse(
                                originalContext.hasReasonResult());

                assertFalse(
                                originalContext.hasAgentPlan());

                assertTrue(
                                originalContext.getExecutionLogs()
                                                .isEmpty());

                verify(
                                agentReasonService).reason(
                                                any(ReasonPromptContext.class));

                verify(
                                agentPlanningService).plan(
                                                any(PlanningPromptContext.class));
        }

        @Test
        void shouldPassReasonResultToPlanningService() {
                ReasonResult reasonResult = ReasonResult.from(
                                "用户希望分析竞业限制条款。");

                AgentPlan agentPlan = AgentPlan.from(
                                List.of(
                                                AgentTask.pending(
                                                                "task-1",
                                                                "读取竞业限制条款"),
                                                AgentTask.pending(
                                                                "task-2",
                                                                "分析条款合法性")));

                when(
                                agentReasonService.reason(
                                                any(ReasonPromptContext.class)))
                                .thenReturn(reasonResult);

                when(
                                agentPlanningService.plan(
                                                any(PlanningPromptContext.class)))
                                .thenReturn(agentPlan);

                AgentPipeline pipeline = new DefaultAgentPipeline(
                                List.of(
                                                reasonOperator,
                                                planningOperator));

                AgentContext result = pipeline.execute(
                                AgentContext.from(
                                                "分析竞业限制条款"));

                verify(
                                agentPlanningService).plan(
                                                org.mockito.ArgumentMatchers.argThat(
                                                                context -> context != null
                                                                                && "分析竞业限制条款"
                                                                                                .equals(
                                                                                                                context.getGoal())
                                                                                && context.getReasonResult() == reasonResult));

                assertSame(
                                reasonResult,
                                result.getReasonResult());

                assertSame(
                                agentPlan,
                                result.getAgentPlan());
        }

        @Test
        void shouldPreserveExistingLogsDuringCompleteFlow() {
                ReasonResult reasonResult = ReasonResult.from(
                                "用户希望审查劳动合同风险。");

                AgentPlan agentPlan = AgentPlan.from(
                                List.of(
                                                AgentTask.pending(
                                                                "task-1",
                                                                "读取劳动合同")));

                when(
                                agentReasonService.reason(
                                                any(ReasonPromptContext.class)))
                                .thenReturn(reasonResult);

                when(
                                agentPlanningService.plan(
                                                any(PlanningPromptContext.class)))
                                .thenReturn(agentPlan);

                AgentPipeline pipeline = new DefaultAgentPipeline(
                                List.of(
                                                planningOperator,
                                                reasonOperator));

                AgentContext initialContext = AgentContext.from(
                                "审查劳动合同风险")
                                .appendExecutionLog(
                                                "Goal validated");

                AgentContext result = pipeline.execute(
                                initialContext);

                assertEquals(
                                List.of(
                                                "Goal validated",
                                                "Reason completed",
                                                "Planning completed"),
                                result.getExecutionLogs());

                assertSame(
                                reasonResult,
                                result.getReasonResult());

                assertSame(
                                agentPlan,
                                result.getAgentPlan());

                assertEquals(
                                AgentStatus.RUNNING,
                                result.getStatus());
        }
}