package com.quince.lawyeraiassistant.rag.vector.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DocumentLoadingService {

    List<Document> loadAndSplit(String location);
}