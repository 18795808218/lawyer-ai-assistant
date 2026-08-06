package com.quince.lawyeraiassistant.agent.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Agent Reason 阶段输出结果。
 *
 * <p>
 * 保存 Agent 对 Goal 的理解结果，
 * 不保存完整 Thought，仅保存可审计的 Reason Summary。
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class ReasonResult {

    /**
     * Agent 对当前 Goal 的理解摘要。
     */
    private final String reasonSummary;

    @Builder(toBuilder = true)
    private ReasonResult(
            String reasonSummary) {
        this.reasonSummary = normalizeReasonSummary(reasonSummary);
    }

    public static ReasonResult from(
            String reasonSummary) {
        return ReasonResult.builder()
                .reasonSummary(reasonSummary)
                .build();
    }

    private static String normalizeReasonSummary(
            String reasonSummary) {

        Objects.requireNonNull(
                reasonSummary,
                "Reason summary must not be null");

        String normalized = reasonSummary.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "Reason summary must not be blank");
        }

        return normalized;
    }
}