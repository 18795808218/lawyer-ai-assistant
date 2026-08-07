package com.quince.lawyeraiassistant.agent.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolExecutionResultTest {

    @Test
    void shouldCreateSuccessfulResult() {

        ToolExecutionResult result = ToolExecutionResult.success(
                "检索到劳动合同法相关规定");

        assertTrue(
                result.isSuccess());

        assertFalse(
                result.isFailure());

        assertEquals(
                "检索到劳动合同法相关规定",
                result.getContent());

        assertNull(
                result.getErrorMessage());
    }

    @Test
    void shouldCreateFailedResult() {

        ToolExecutionResult result = ToolExecutionResult.failure(
                "VectorStore unavailable");

        assertFalse(
                result.isSuccess());

        assertTrue(
                result.isFailure());

        assertNull(
                result.getContent());

        assertEquals(
                "VectorStore unavailable",
                result.getErrorMessage());
    }

    @Test
    void shouldTrimContent() {

        ToolExecutionResult result = ToolExecutionResult.success(
                "  检索成功  ");

        assertEquals(
                "检索成功",
                result.getContent());
    }

    @Test
    void shouldTrimErrorMessage() {

        ToolExecutionResult result = ToolExecutionResult.failure(
                "  VectorStore unavailable  ");

        assertEquals(
                "VectorStore unavailable",
                result.getErrorMessage());
    }

    @Test
    void shouldRejectBlankSuccessfulContent() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolExecutionResult.success(
                        "   "));

        assertEquals(
                "Successful tool result must contain content",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankFailureMessage() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolExecutionResult.failure(
                        "   "));

        assertEquals(
                "Failed tool result must contain errorMessage",
                exception.getMessage());
    }

    @Test
    void shouldRejectSuccessfulResultWithErrorMessage() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolExecutionResult.builder()
                        .success(true)
                        .content("result")
                        .errorMessage("error")
                        .build());

        assertEquals(
                "Successful tool result must not contain errorMessage",
                exception.getMessage());
    }

    @Test
    void shouldRejectFailedResultWithContent() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolExecutionResult.builder()
                        .success(false)
                        .content("result")
                        .errorMessage("error")
                        .build());

        assertEquals(
                "Failed tool result must not contain content",
                exception.getMessage());
    }
}