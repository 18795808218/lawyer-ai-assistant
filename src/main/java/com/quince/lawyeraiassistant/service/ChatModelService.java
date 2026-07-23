package com.quince.lawyeraiassistant.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.quince.lawyeraiassistant.dto.ChatDetailResponse;
import com.quince.lawyeraiassistant.dto.ConversationMessageResponse;
import com.quince.lawyeraiassistant.dto.MultiTurnChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.quince.lawyeraiassistant.dto.PromptInspectResponse;
import com.quince.lawyeraiassistant.dto.PromptMessageResponse;
import com.quince.lawyeraiassistant.prompt.PromptPaths;
import com.quince.lawyeraiassistant.prompt.PromptResourceLoader;
import com.quince.lawyeraiassistant.dto.LegalAnalysisRequest;
import com.quince.lawyeraiassistant.dto.LegalAnalysisResponse;

@Service
public class ChatModelService {

    private final ChatModel chatModel;
    private final String lawyerSystemPrompt;
    private final PromptTemplate caseAnalysisTemplate;

    public ChatModelService(
            ChatModel chatModel,
            PromptResourceLoader promptResourceLoader) {

        this.chatModel = chatModel;

        this.lawyerSystemPrompt = promptResourceLoader.load(
                PromptPaths.LAWYER_SYSTEM);

        Resource caseAnalysisResource = promptResourceLoader.getResource(
                PromptPaths.CASE_ANALYSIS);

        this.caseAnalysisTemplate = PromptTemplate
                .builder()
                .resource(caseAnalysisResource)
                .build();
    }

    public String chat(String message) {
        ChatResponse response = callModel(message);

        return response
                .getResult()
                .getOutput()
                .getText();
    }

    public ChatDetailResponse chatDetail(String message) {
        ChatResponse response = callModel(message);

        var result = response.getResult();
        var usage = response.getMetadata().getUsage();

        return new ChatDetailResponse(
                result.getOutput().getText(),
                String.valueOf(result.getMetadata().getFinishReason()),
                usage == null ? null : usage.getPromptTokens(),
                usage == null ? null : usage.getCompletionTokens(),
                usage == null ? null : usage.getTotalTokens(),
                String.valueOf(response.getMetadata().getModel()),
                String.valueOf(result.getMetadata()),
                String.valueOf(response.getMetadata()));
    }

    private ChatResponse callModel(String message) {
        SystemMessage systemMessage = new SystemMessage(lawyerSystemPrompt);

        UserMessage userMessage = new UserMessage(message);

        Prompt prompt = new Prompt(
                List.of(systemMessage, userMessage));

        return chatModel.call(prompt);
    }

    public PromptInspectResponse inspectPrompt(String message) {
        SystemMessage systemMessage = new SystemMessage(lawyerSystemPrompt);

        UserMessage userMessage = new UserMessage(message);

        Prompt prompt = new Prompt(
                List.of(systemMessage, userMessage));

        System.out.println("========== Prompt 开始 ==========");
        System.out.println(prompt);
        System.out.println("========== Prompt 结束 ==========");

        List<PromptMessageResponse> promptMessages = prompt.getInstructions()
                .stream()
                .map(item -> new PromptMessageResponse(
                        String.valueOf(item.getMessageType()),
                        item.getText()))
                .toList();

        return new PromptInspectResponse(
                prompt.toString(),
                promptMessages.size(),
                promptMessages);
    }

    public MultiTurnChatResponse multiTurnChat(
            String firstMessage,
            String secondMessage) {

        List<Message> messages = new ArrayList<>();

        // 1. 添加系统消息
        messages.add(new SystemMessage(lawyerSystemPrompt));

        // 2. 添加用户第一轮问题
        messages.add(new UserMessage(firstMessage));

        // 3. 发起第一轮模型调用
        Prompt firstPrompt = new Prompt(
                new ArrayList<>(messages));

        ChatResponse firstResponse = chatModel.call(firstPrompt);

        var firstAssistantMessage = firstResponse
                .getResult()
                .getOutput();

        // 4. 将第一轮模型回答加入历史消息
        messages.add(firstAssistantMessage);

        // 5. 添加用户第二轮问题
        messages.add(new UserMessage(secondMessage));

        // 6. 携带完整历史，发起第二轮模型调用
        Prompt secondPrompt = new Prompt(
                new ArrayList<>(messages));

        ChatResponse secondResponse = chatModel.call(secondPrompt);

        String firstAnswer = firstAssistantMessage.getText();

        String secondAnswer = secondResponse
                .getResult()
                .getOutput()
                .getText();

        List<ConversationMessageResponse> conversationHistory = messages.stream()
                .map(item -> new ConversationMessageResponse(
                        String.valueOf(item.getMessageType()),
                        item.getText()))
                .toList();

        return new MultiTurnChatResponse(
                firstAnswer,
                secondAnswer,
                messages.size(),
                conversationHistory);
    }

    public LegalAnalysisResponse analyzeCase(
            LegalAnalysisRequest request) {

        validateLegalAnalysisRequest(request);

        Map<String, Object> variables = Map.of(
                "caseType", request.caseType().strip(),
                "clientRole", request.clientRole().strip(),
                "caseDescription", request.caseDescription().strip());

        String renderedUserPrompt = caseAnalysisTemplate.render(variables);

        SystemMessage systemMessage = new SystemMessage(lawyerSystemPrompt);

        UserMessage userMessage = new UserMessage(renderedUserPrompt);

        Prompt prompt = new Prompt(
                List.of(systemMessage, userMessage));

        System.out.println(
                "========== 渲染后的 User Prompt ==========");
        System.out.println(renderedUserPrompt);
        System.out.println(
                "==========================================");

        ChatResponse response = chatModel.call(prompt);

        var result = response.getResult();
        var usage = response.getMetadata().getUsage();

        return new LegalAnalysisResponse(
                result.getOutput().getText(),
                renderedUserPrompt,
                usage == null ? null : usage.getPromptTokens(),
                usage == null ? null : usage.getCompletionTokens(),
                usage == null ? null : usage.getTotalTokens());
    }

    private void validateLegalAnalysisRequest(
            LegalAnalysisRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "请求内容不能为空");
        }

        if (request.caseType() == null
                || request.caseType().isBlank()) {

            throw new IllegalArgumentException(
                    "案件类型不能为空");
        }

        if (request.clientRole() == null
                || request.clientRole().isBlank()) {

            throw new IllegalArgumentException(
                    "当事人身份不能为空");
        }

        if (request.caseDescription() == null
                || request.caseDescription().isBlank()) {

            throw new IllegalArgumentException(
                    "案件描述不能为空");
        }

        if (request.caseDescription().length() > 10_000) {
            throw new IllegalArgumentException(
                    "案件描述不能超过10000个字符");
        }
    }
}