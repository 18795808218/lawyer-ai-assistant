package com.quince.lawyeraiassistant.dto;

public record LegalAnalysisRequest(
        String caseType,
        String clientRole,
        String caseDescription) {
}