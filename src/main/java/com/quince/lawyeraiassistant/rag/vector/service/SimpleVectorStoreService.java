package com.quince.lawyeraiassistant.rag.vector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleVectorStoreService {

    private final VectorStore vectorStore;

    public int addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }

        vectorStore.add(documents);
        return documents.size();
    }

    public List<Document> search(String query) {

        log.info("Similarity Search : {}", query);
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(5)
                .build();

        List<Document> documents = vectorStore.similaritySearch(request);
        log.info("Found {} similar documents", documents.size());
        return documents;
    }
}