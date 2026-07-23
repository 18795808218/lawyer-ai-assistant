package com.quince.lawyeraiassistant.dto;

public record LegalAnalysisResponse(
        String answer,
        String renderedPrompt,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens) {
}