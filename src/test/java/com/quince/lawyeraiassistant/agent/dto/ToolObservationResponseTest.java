package com.quince.lawyeraiassistant.agent.dto;

import com.quince.lawyeraiassistant.agent.model.ToolObservation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolObservationResponseTest {

    @Test
    void shouldCreateResponseFromSuccessfulObservation() {

        ToolObservation observation = ToolObservation.success(
                "task-1",
                "searchLegalKnowledge",
                "检索到劳动合同法相关规定。");

        ToolObservationResponse response = ToolObservationResponse.from(
                observation);

        assertEquals(
                "task-1",
                response.taskId());

        assertEquals(
                "searchLegalKnowledge",
                response.toolName());

        assertTrue(
                response.success());

        assertEquals(
                "检索到劳动合同法相关规定。",
                response.content());

        assertNull(
                response.errorMessage());
    }

    @Test
    void shouldCreateResponseFromFailedObservation() {

        ToolObservation observation = ToolObservation.failure(
                "task-1",
                "searchLegalKnowledge",
                "VectorStore unavailable");

        ToolObservationResponse response = ToolObservationResponse.from(
                observation);

        assertEquals(
                "task-1",
                response.taskId());

        assertEquals(
                "searchLegalKnowledge",
                response.toolName());

        assertFalse(
                response.success());

        assertNull(
                response.content());

        assertEquals(
                "VectorStore unavailable",
                response.errorMessage());
    }

    @Test
    void shouldRejectNullObservation() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ToolObservationResponse.from(
                        null));

        assertEquals(
                "ToolObservation must not be null",
                exception.getMessage());
    }
}