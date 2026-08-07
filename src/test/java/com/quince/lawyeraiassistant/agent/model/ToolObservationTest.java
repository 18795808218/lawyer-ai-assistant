package com.quince.lawyeraiassistant.agent.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolObservationTest {

    @Test
    void shouldCreateSuccessfulObservation() {

        ToolObservation observation = ToolObservation.success(
                "task-1",
                "searchLegalKnowledge",
                "检索到劳动合同法相关规定");

        assertEquals(
                "task-1",
                observation.getTaskId());

        assertEquals(
                "searchLegalKnowledge",
                observation.getToolName());

        assertTrue(
                observation.isSuccess());

        assertFalse(
                observation.isFailure());

        assertEquals(
                "检索到劳动合同法相关规定",
                observation.getContent());

        assertNull(
                observation.getErrorMessage());
    }

    @Test
    void shouldCreateFailedObservation() {

        ToolObservation observation = ToolObservation.failure(
                "task-1",
                "searchLegalKnowledge",
                "VectorStore unavailable");

        assertEquals(
                "task-1",
                observation.getTaskId());

        assertEquals(
                "searchLegalKnowledge",
                observation.getToolName());

        assertFalse(
                observation.isSuccess());

        assertTrue(
                observation.isFailure());

        assertNull(
                observation.getContent());

        assertEquals(
                "VectorStore unavailable",
                observation.getErrorMessage());
    }

    @Test
    void shouldTrimFields() {

        ToolObservation observation = ToolObservation.success(
                "  task-1  ",
                "  searchLegalKnowledge  ",
                "  检索成功  ");

        assertEquals(
                "task-1",
                observation.getTaskId());

        assertEquals(
                "searchLegalKnowledge",
                observation.getToolName());

        assertEquals(
                "检索成功",
                observation.getContent());
    }

    @Test
    void shouldRejectNullTaskId() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ToolObservation.success(
                        null,
                        "searchLegalKnowledge",
                        "result"));

        assertEquals(
                "Task id must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankTaskId() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolObservation.success(
                        "   ",
                        "searchLegalKnowledge",
                        "result"));

        assertEquals(
                "Task id must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullToolName() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ToolObservation.success(
                        "task-1",
                        null,
                        "result"));

        assertEquals(
                "Tool name must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankToolName() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolObservation.success(
                        "task-1",
                        "   ",
                        "result"));

        assertEquals(
                "Tool name must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankSuccessfulContent() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolObservation.success(
                        "task-1",
                        "searchLegalKnowledge",
                        "   "));

        assertEquals(
                "Successful observation must contain content",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankFailureMessage() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolObservation.failure(
                        "task-1",
                        "searchLegalKnowledge",
                        "   "));

        assertEquals(
                "Failed observation must contain errorMessage",
                exception.getMessage());
    }

    @Test
    void shouldRejectSuccessfulObservationWithErrorMessage() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolObservation.builder()
                        .taskId("task-1")
                        .toolName("searchLegalKnowledge")
                        .success(true)
                        .content("result")
                        .errorMessage("error")
                        .build());

        assertEquals(
                "Successful observation must not contain errorMessage",
                exception.getMessage());
    }

    @Test
    void shouldRejectFailedObservationWithContent() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolObservation.builder()
                        .taskId("task-1")
                        .toolName("searchLegalKnowledge")
                        .success(false)
                        .content("result")
                        .errorMessage("error")
                        .build());

        assertEquals(
                "Failed observation must not contain content",
                exception.getMessage());
    }
}