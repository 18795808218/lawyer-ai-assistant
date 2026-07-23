package com.quince.lawyeraiassistant.dto;

public record ChatDetailResponse(
        String content,
        String finishReason,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        String model,
        String resultMetadata,
        String responseMetadata) {
}