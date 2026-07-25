package com.quince.lawyeraiassistant.dto.response;

public class ChatMemoryResponse {

    private String conversationId;

    private String answer;

    public ChatMemoryResponse() {
    }

    public ChatMemoryResponse(
            String conversationId,
            String answer) {

        this.conversationId = conversationId;
        this.answer = answer;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}