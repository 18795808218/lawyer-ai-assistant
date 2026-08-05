package com.quince.lawyeraiassistant.agent.pipeline;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.operator.AgentOperator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AgentPipeline 的默认实现。
 *
 * <p>
 * Spring 会自动注入所有 AgentOperator Bean。
 * Pipeline 根据 {@code @Order} 或 {@code Ordered}
 * 定义的顺序依次执行各个节点。
 * </p>
 *
 * <pre>
 * AgentContext
 *      ↓
 * ReasonOperator
 *      ↓
 * PlanningOperator
 *      ↓
 * ToolOperator
 *      ↓
 * ReflectionOperator
 *      ↓
 * Final AgentContext
 * </pre>
 */
@Component
public class DefaultAgentPipeline
        implements AgentPipeline {

    private final List<AgentOperator> operators;

    public DefaultAgentPipeline(
            List<AgentOperator> operators) {
        Objects.requireNonNull(
                operators,
                "AgentOperator list must not be null");

        List<AgentOperator> sortedOperators = new ArrayList<>(operators);

        validateOperators(sortedOperators);

        AnnotationAwareOrderComparator.sort(
                sortedOperators);

        this.operators = List.copyOf(sortedOperators);
    }

    @Override
    public AgentContext execute(
            AgentContext context) {
        Objects.requireNonNull(
                context,
                "AgentContext must not be null");

        AgentContext currentContext = context;

        for (AgentOperator operator : operators) {
            currentContext = operator.execute(
                    currentContext);

            if (currentContext == null) {
                throw new IllegalStateException(
                        "AgentOperator must not return null: "
                                + operator
                                        .getClass()
                                        .getName());
            }
        }

        return currentContext;
    }

    private void validateOperators(
            List<AgentOperator> operators) {
        for (AgentOperator operator : operators) {
            if (operator == null) {
                throw new IllegalArgumentException(
                        "AgentOperator list must not contain null");
            }
        }
    }
}