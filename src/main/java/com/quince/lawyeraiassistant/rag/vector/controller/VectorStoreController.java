package com.quince.lawyeraiassistant.rag.vector.controller;

import com.quince.lawyeraiassistant.rag.vector.service.SimpleVectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VectorStoreController {

    private final SimpleVectorStoreService vectorStoreService;

    @GetMapping("/rag/search")
    public List<Document> search(
            @RequestParam String question) {

        return vectorStoreService.search(question);

    }

}