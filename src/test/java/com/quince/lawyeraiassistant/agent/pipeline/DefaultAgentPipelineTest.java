package com.quince.lawyeraiassistant.agent.pipeline;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.operator.AgentOperator;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultAgentPipelineTest {

    @Test
    void shouldExecuteOperatorsInOrder() {
        List<String> executionOrder = new ArrayList<>();

        AgentOperator firstOperator = context -> {
            executionOrder.add("first");

            return context
                    .appendExecutionLog(
                            "first completed");
        };

        AgentOperator secondOperator = context -> {
            executionOrder.add("second");

            return context
                    .appendExecutionLog(
                            "second completed");
        };

        AgentPipeline pipeline = new DefaultAgentPipeline(
                List.of(
                        firstOperator,
                        secondOperator));

        AgentContext result = pipeline.execute(
                AgentContext.from(
                        "分析劳动合同"));

        assertEquals(
                List.of("first", "second"),
                executionOrder);

        assertEquals(
                List.of(
                        "first completed",
                        "second completed"),
                result.getExecutionLogs());
    }

    @Test
    void shouldPassPreviousResultToNextOperator() {
        AgentOperator runningOperator = context -> context.toBuilder()
                .status(
                        AgentStatus.RUNNING)
                .build();

        AgentOperator finishOperator = context -> {
            assertTrue(
                    context.isRunning());

            return context.toBuilder()
                    .status(
                            AgentStatus.FINISHED)
                    .build();
        };

        AgentPipeline pipeline = new DefaultAgentPipeline(
                List.of(
                        runningOperator,
                        finishOperator));

        AgentContext result = pipeline.execute(
                AgentContext.from(
                        "生成律师意见书"));

        assertTrue(result.isFinished());
    }

    @Test
    void shouldPreserveOriginalContextImmutability() {
        AgentContext originalContext = AgentContext.from(
                "分析劳动合同");

        AgentOperator operator = context -> context.toBuilder()
                .status(
                        AgentStatus.RUNNING)
                .build()
                .appendExecutionLog(
                        "Reason completed");

        AgentPipeline pipeline = new DefaultAgentPipeline(
                List.of(operator));

        AgentContext result = pipeline.execute(
                originalContext);

        assertNotSame(
                originalContext,
                result);

        assertEquals(
                AgentStatus.CREATED,
                originalContext.getStatus());

        assertTrue(
                originalContext
                        .getExecutionLogs()
                        .isEmpty());

        assertEquals(
                AgentStatus.RUNNING,
                result.getStatus());

        assertEquals(
                List.of(
                        "Reason completed"),
                result.getExecutionLogs());
    }

    @Test
    void shouldReturnOriginalContextWhenPipelineIsEmpty() {
        AgentPipeline pipeline = new DefaultAgentPipeline(
                List.of());

        AgentContext context = AgentContext.from(
                "测试目标");

        AgentContext result = pipeline.execute(context);

        assertSame(context, result);
    }

    @Test
    void shouldSortOperatorsByOrderAnnotation() {
        List<String> executionOrder = new ArrayList<>();

        AgentOperator laterOperator = new LaterOperator(
                executionOrder);

        AgentOperator earlierOperator = new EarlierOperator(
                executionOrder);

        AgentPipeline pipeline = new DefaultAgentPipeline(
                List.of(
                        laterOperator,
                        earlierOperator));

        pipeline.execute(
                AgentContext.from(
                        "测试目标"));

        assertEquals(
                List.of(
                        "earlier",
                        "later"),
                executionOrder);
    }

    @Test
    void shouldThrowExceptionWhenContextIsNull() {
        AgentPipeline pipeline = new DefaultAgentPipeline(
                List.of());

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> pipeline.execute(null));

        assertEquals(
                "AgentContext must not be null",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOperatorListIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DefaultAgentPipeline(
                        null));

        assertEquals(
                "AgentOperator list must not be null",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOperatorListContainsNull() {
        List<AgentOperator> operators = Arrays.asList(
                context -> context,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new DefaultAgentPipeline(
                        operators));

        assertEquals(
                "AgentOperator list must not contain null",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOperatorReturnsNull() {
        AgentOperator invalidOperator = context -> null;

        AgentPipeline pipeline = new DefaultAgentPipeline(
                List.of(
                        invalidOperator));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> pipeline.execute(
                        AgentContext.from(
                                "测试目标")));

        assertTrue(
                exception.getMessage()
                        .startsWith(
                                "AgentOperator must not return null:"));
    }

    @Test
    void shouldPropagateOperatorException() {
        IllegalStateException expectedException = new IllegalStateException(
                "Agent execution failed");

        AgentOperator operator = context -> {
            throw expectedException;
        };

        AgentPipeline pipeline = new DefaultAgentPipeline(
                List.of(operator));

        IllegalStateException actualException = assertThrows(
                IllegalStateException.class,
                () -> pipeline.execute(
                        AgentContext.from(
                                "测试目标")));

        assertSame(
                expectedException,
                actualException);
    }

    @Order(100)
    private static class EarlierOperator
            implements AgentOperator {

        private final List<String> executionOrder;

        private EarlierOperator(
                List<String> executionOrder) {
            this.executionOrder = executionOrder;
        }

        @Override
        public AgentContext execute(
                AgentContext context) {
            executionOrder.add("earlier");
            return context;
        }
    }

    @Order(200)
    private static class LaterOperator
            implements AgentOperator {

        private final List<String> executionOrder;

        private LaterOperator(
                List<String> executionOrder) {
            this.executionOrder = executionOrder;
        }

        @Override
        public AgentContext execute(
                AgentContext context) {
            executionOrder.add("later");
            return context;
        }
    }
}