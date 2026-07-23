package com.quince.lawyeraiassistant.dto;

import java.util.List;

public record PromptInspectResponse(
        String promptText,
        int messageCount,
        List<PromptMessageResponse> messages
) {
}