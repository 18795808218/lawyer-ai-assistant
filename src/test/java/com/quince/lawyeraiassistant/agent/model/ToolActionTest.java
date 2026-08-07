package com.quince.lawyeraiassistant.agent.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolActionTest {

    @Test
    void shouldCreateToolActionWithArguments() {

        ToolAction action = ToolAction.of(
                "task-1",
                "searchLegalKnowledge",
                Map.of(
                        "legalQuestion",
                        "劳动合同违法解除的规定"));

        assertEquals(
                "task-1",
                action.getTaskId());

        assertEquals(
                "searchLegalKnowledge",
                action.getToolName());

        assertEquals(
                "劳动合同违法解除的规定",
                action.getArguments()
                        .get("legalQuestion"));

        assertTrue(
                action.hasArguments());
    }

    @Test
    void shouldCreateToolActionWithoutArguments() {

        ToolAction action = ToolAction.of(
                "task-1",
                "readCurrentDocument");

        assertEquals(
                "task-1",
                action.getTaskId());

        assertEquals(
                "readCurrentDocument",
                action.getToolName());

        assertTrue(
                action.getArguments()
                        .isEmpty());

        assertFalse(
                action.hasArguments());
    }

    @Test
    void shouldNormalizeNullArgumentsToEmptyMap() {

        ToolAction action = ToolAction.of(
                "task-1",
                "searchLegalKnowledge",
                null);

        assertTrue(
                action.getArguments()
                        .isEmpty());
    }

    @Test
    void shouldTrimTaskIdAndToolName() {

        ToolAction action = ToolAction.of(
                "  task-1  ",
                "  searchLegalKnowledge  ");

        assertEquals(
                "task-1",
                action.getTaskId());

        assertEquals(
                "searchLegalKnowledge",
                action.getToolName());
    }

    @Test
    void shouldCreateDefensiveCopyOfArguments() {

        Map<String, Object> arguments = new HashMap<>();

        arguments.put(
                "legalQuestion",
                "劳动合同解除");

        ToolAction action = ToolAction.of(
                "task-1",
                "searchLegalKnowledge",
                arguments);

        arguments.clear();

        assertEquals(
                "劳动合同解除",
                action.getArguments()
                        .get("legalQuestion"));
    }

    @Test
    void shouldExposeUnmodifiableArguments() {

        ToolAction action = ToolAction.of(
                "task-1",
                "searchLegalKnowledge",
                Map.of(
                        "legalQuestion",
                        "劳动合同解除"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> action.getArguments()
                        .put(
                                "another",
                                "value"));
    }

    @Test
    void shouldRejectNullTaskId() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ToolAction.of(
                        null,
                        "searchLegalKnowledge"));

        assertEquals(
                "Task id must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankTaskId() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolAction.of(
                        "   ",
                        "searchLegalKnowledge"));

        assertEquals(
                "Task id must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectNullToolName() {

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ToolAction.of(
                        "task-1",
                        null));

        assertEquals(
                "Tool name must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankToolName() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ToolAction.of(
                        "task-1",
                        "   "));

        assertEquals(
                "Tool name must not be blank",
                exception.getMessage());
    }
}