package com.quince.lawyeraiassistant.rag.splitter;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LaborLawSplitter {

    private final LegalTextSplitter legalTextSplitter;

    public List<Document> split(List<Document> documents) {

        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return legalTextSplitter.apply(documents);
    }
}