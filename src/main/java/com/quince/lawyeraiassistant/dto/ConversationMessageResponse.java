package com.quince.lawyeraiassistant.dto;

public record ConversationMessageResponse(
        String messageType,
        String text) {
}