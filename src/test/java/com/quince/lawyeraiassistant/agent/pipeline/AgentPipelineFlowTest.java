package com.quince.lawyeraiassistant.agent.pipeline;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.operator.DummyPlanningOperator;
import com.quince.lawyeraiassistant.agent.operator.DummyReasonOperator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Agent Pipeline 的完整流程测试。
 *
 * <p>
 * 本测试不启动 Spring 容器，也不连接真实 LLM 或 Tool。
 * 它通过真实的 DefaultAgentPipeline、DummyReasonOperator
 * 和 DummyPlanningOperator，验证多个 Agent 节点组合后的执行行为。
 * </p>
 *
 * <p>验证流程：</p>
 *
 * <pre>
 * AgentContext(CREATED)
 *          ↓
 * DummyReasonOperator
 *          ↓
 * AgentContext(RUNNING)
 *          ↓
 * DummyPlanningOperator
 *          ↓
 * Final AgentContext
 * </pre>
 */
class AgentPipelineFlowTest {

    @Test
    void shouldExecuteCompleteAgentFlow() {
        /*
         * 故意将 Planning 放在 Reason 前面，
         * 用于验证 DefaultAgentPipeline 会根据 @Order 自动排序。
         */
        AgentPipeline pipeline =
                new DefaultAgentPipeline(
                        List.of(
                                new DummyPlanningOperator(),
                                new DummyReasonOperator()
                        )
                );

        AgentContext originalContext =
                AgentContext.from(
                        "分析劳动合同并生成律师意见书"
                );

        AgentContext result =
                pipeline.execute(
                        originalContext
                );

        /*
         * Pipeline 执行后应该返回新的 Context，
         * 不允许直接修改原对象。
         */
        assertNotSame(
                originalContext,
                result
        );

        /*
         * DummyReasonOperator 应将状态从 CREATED 更新为 RUNNING。
         */
        assertEquals(
                AgentStatus.RUNNING,
                result.getStatus()
        );

        /*
         * 即使构造 Pipeline 时顺序为：
         *
         * Planning → Reason
         *
         * 实际执行仍应根据 @Order 变为：
         *
         * Reason → Planning
         */
        assertEquals(
                List.of(
                        "Reason completed",
                        "Planning completed"
                ),
                result.getExecutionLogs()
        );

        /*
         * 原始 Context 必须保持不变。
         */
        assertEquals(
                AgentStatus.CREATED,
                originalContext.getStatus()
        );

        assertEquals(
                List.of(),
                originalContext.getExecutionLogs()
        );

        /*
         * Goal 在整个 Pipeline 中不能丢失。
         */
        assertEquals(
                "分析劳动合同并生成律师意见书",
                result.getGoal()
        );
    }

    @Test
    void shouldPassReasonResultToPlanningOperator() {
        AgentPipeline pipeline =
                new DefaultAgentPipeline(
                        List.of(
                                new DummyReasonOperator(),
                                new DummyPlanningOperator()
                        )
                );

        AgentContext result =
                pipeline.execute(
                        AgentContext.from(
                                "审查劳动合同风险"
                        )
                );

        /*
         * Planning 执行后，Reason 设置的 RUNNING 状态仍然存在，
         * 说明前一个 Operator 的结果被传递给了下一个 Operator。
         */
        assertEquals(
                AgentStatus.RUNNING,
                result.getStatus()
        );

        assertEquals(
                List.of(
                        "Reason completed",
                        "Planning completed"
                ),
                result.getExecutionLogs()
        );
    }

    @Test
    void shouldPreserveGoalDuringCompleteFlow() {
        String goal =
                "分析竞业限制条款并生成修改建议";

        AgentPipeline pipeline =
                new DefaultAgentPipeline(
                        List.of(
                                new DummyPlanningOperator(),
                                new DummyReasonOperator()
                        )
                );

        AgentContext result =
                pipeline.execute(
                        AgentContext.from(goal)
                );

        assertEquals(
                goal,
                result.getGoal()
        );

        assertEquals(
                AgentStatus.RUNNING,
                result.getStatus()
        );

        assertEquals(
                2,
                result.executionLogCount()
        );
    }
}