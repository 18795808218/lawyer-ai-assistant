package com.quince.lawyeraiassistant.dto;

public record PromptMessageResponse(
        String messageType,
        String text) {
}