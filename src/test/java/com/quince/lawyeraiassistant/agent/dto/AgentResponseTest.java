package com.quince.lawyeraiassistant.agent.dto;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentResponseTest {

    @Test
    void shouldCreateResponseFromAgentContext() {
        AgentContext context = AgentContext.builder()
                .goal(
                        "分析劳动合同")
                .status(
                        AgentStatus.RUNNING)
                .executionLogs(
                        List.of(
                                "Reason completed",
                                "Planning completed"))
                .build();

        AgentResponse response = AgentResponse.from(context);

        assertEquals(
                "分析劳动合同",
                response.goal());

        assertEquals(
                AgentStatus.RUNNING,
                response.status());

        assertEquals(
                List.of(
                        "Reason completed",
                        "Planning completed"),
                response.executionLogs());
    }

    @Test
    void shouldRejectNullContext() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> AgentResponse.from(null));

        assertEquals(
                "AgentContext must not be null",
                exception.getMessage());
    }
}