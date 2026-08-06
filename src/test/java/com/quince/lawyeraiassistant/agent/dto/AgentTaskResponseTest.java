package com.quince.lawyeraiassistant.agent.dto;

import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.AgentTaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentTaskResponseTest {

    @Test
    void shouldCreateResponseFromAgentTask() {
        AgentTask task = AgentTask.pending(
                "task-1",
                "读取劳动合同");

        AgentTaskResponse response = AgentTaskResponse.from(
                task);

        assertEquals(
                "task-1",
                response.id());

        assertEquals(
                "读取劳动合同",
                response.description());

        assertEquals(
                AgentTaskStatus.PENDING,
                response.status());
    }

    @Test
    void shouldRejectNullTask() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> AgentTaskResponse.from(
                        null));

        assertEquals(
                "AgentTask must not be null",
                exception.getMessage());
    }
}