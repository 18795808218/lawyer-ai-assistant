package com.quince.lawyeraiassistant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.quince.lawyeraiassistant.dto.ChatDetailResponse;
import com.quince.lawyeraiassistant.dto.LegalAnalysisRequest;
import com.quince.lawyeraiassistant.dto.LegalAnalysisResponse;
import com.quince.lawyeraiassistant.dto.MultiTurnChatResponse;
import com.quince.lawyeraiassistant.dto.PromptInspectResponse;
import com.quince.lawyeraiassistant.service.ChatModelService;
import com.quince.lawyeraiassistant.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatModelService chatModelService;

    public ChatController(
            ChatService chatService,
            ChatModelService chatModelService) {

        this.chatService = chatService;
        this.chatModelService = chatModelService;
    }

    @GetMapping("/client")
    public String chatByClient(@RequestParam String message) {
        return chatService.chat(message);
    }

    @GetMapping("/model")
    public String chatByModel(@RequestParam String message) {
        return chatModelService.chat(message);
    }

    @GetMapping("/model/detail")
    public ChatDetailResponse chatDetailByModel(
            @RequestParam String message) {

        return chatModelService.chatDetail(message);
    }

    @GetMapping("/model/prompt")
    public PromptInspectResponse inspectPrompt(
            @RequestParam String message) {

        return chatModelService.inspectPrompt(message);
    }

    @GetMapping("/model/multi-turn")
    public MultiTurnChatResponse multiTurnChat(
            @RequestParam String firstMessage,
            @RequestParam String secondMessage) {

        return chatModelService.multiTurnChat(
                firstMessage,
                secondMessage);
    }

    @PostMapping("/model/template")
    public LegalAnalysisResponse analyzeCase(
            @RequestBody LegalAnalysisRequest request) {

        return chatModelService.analyzeCase(request);
    }
}