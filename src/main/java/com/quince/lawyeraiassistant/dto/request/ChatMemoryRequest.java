package com.quince.lawyeraiassistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatMemoryRequest {

    @NotBlank(message = "conversationId 不能为空")
    @Size(max = 100, message = "conversationId 长度不能超过 100 个字符")
    private String conversationId;

    @NotBlank(message = "message 不能为空")
    @Size(max = 5000, message = "message 长度不能超过 5000 个字符")
    private String message;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}