package com.quince.lawyeraiassistant.agent.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.quince.lawyeraiassistant.agent.model.AgentContext;

public class DummyReasonOperatorTest {
    
    @Test
    void shouldMarkContextAsRunning() {

        DummyReasonOperator operator = new DummyReasonOperator();

        AgentContext result = operator.execute(
                AgentContext.from(
                        "分析劳动合同"));

        assertTrue(
                result.isRunning());

        assertEquals(
                List.of(
                        "Reason completed"),
                result.getExecutionLogs());
    }
}
