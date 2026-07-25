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
import com.quince.lawyeraiassistant.dto.request.ChatMemoryRequest;
import com.quince.lawyeraiassistant.dto.response.ChatMemoryResponse;
import com.quince.lawyeraiassistant.service.ChatMemoryService;
import com.quince.lawyeraiassistant.service.ChatModelService;
import com.quince.lawyeraiassistant.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatModelService chatModelService;
    private final ChatMemoryService chatMemoryService;

    public ChatController(
            ChatService chatService,
            ChatModelService chatModelService,
            ChatMemoryService chatMemoryService) {

        this.chatService = chatService;
        this.chatModelService = chatModelService;
        this.chatMemoryService = chatMemoryService;
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

    @PostMapping("/memory")
    public ChatMemoryResponse chatMemory(
            @Valid @RequestBody ChatMemoryRequest request) {

        return chatMemoryService.chat(request);
    }
}