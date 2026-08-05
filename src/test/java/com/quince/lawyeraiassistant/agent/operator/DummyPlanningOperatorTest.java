package com.quince.lawyeraiassistant.agent.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.quince.lawyeraiassistant.agent.model.AgentContext;

public class DummyPlanningOperatorTest {
    
    @Test
    void shouldAppendPlanningLog() {

        DummyPlanningOperator operator = new DummyPlanningOperator();

        AgentContext context = AgentContext.from(
                "分析劳动合同");

        AgentContext result = operator.execute(context);

        assertEquals(
                List.of(
                        "Planning completed"),
                result.getExecutionLogs());
    }
}
