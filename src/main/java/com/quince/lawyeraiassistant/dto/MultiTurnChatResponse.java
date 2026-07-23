package com.quince.lawyeraiassistant.dto;

import java.util.List;

public record MultiTurnChatResponse(
        String firstAssistantAnswer,
        String secondAssistantAnswer,
        int historyMessageCount,
        List<ConversationMessageResponse> conversationHistory) {
}