package com.quince.lawyeraiassistant.agent.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReasonResultTest {

    @Test
    void shouldCreateReasonResultFromSummary() {
        ReasonResult result = ReasonResult.from(
                "用户希望分析劳动合同并生成律师意见书");

        assertEquals(
                "用户希望分析劳动合同并生成律师意见书",
                result.getReasonSummary());
    }

    @Test
    void shouldTrimReasonSummary() {
        ReasonResult result = ReasonResult.from(
                "  用户希望分析劳动合同  ");

        assertEquals(
                "用户希望分析劳动合同",
                result.getReasonSummary());
    }

    @Test
    void shouldCreateNewInstanceWithToBuilder() {
        ReasonResult original = ReasonResult.from(
                "用户希望分析劳动合同");

        ReasonResult updated = original.toBuilder()
                .reasonSummary(
                        "用户希望分析劳动合同并生成律师意见书")
                .build();

        assertNotSame(
                original,
                updated);

        assertEquals(
                "用户希望分析劳动合同",
                original.getReasonSummary());

        assertEquals(
                "用户希望分析劳动合同并生成律师意见书",
                updated.getReasonSummary());
    }

    @Test
    void shouldRejectNullReasonSummary() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> ReasonResult.from(null));

        assertEquals(
                "Reason summary must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankReasonSummary() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ReasonResult.from("   "));

        assertEquals(
                "Reason summary must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        ReasonResult first = ReasonResult.from(
                "用户希望分析劳动合同");

        ReasonResult second = ReasonResult.from(
                "用户希望分析劳动合同");

        assertEquals(
                first,
                second);

        assertEquals(
                first.hashCode(),
                second.hashCode());
    }
}