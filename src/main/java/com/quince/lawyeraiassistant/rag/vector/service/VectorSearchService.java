package com.quince.lawyeraiassistant.rag.vector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.quince.lawyeraiassistant.common.exception.ErrorCode;
import com.quince.lawyeraiassistant.common.exception.KnowledgeBaseException;
import com.quince.lawyeraiassistant.rag.config.RetrievalProperties;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final VectorStore vectorStore;
    private final RetrievalProperties retrievalProperties;

    public int addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }

        try {
            vectorStore.add(documents);
            return documents.size();
        } catch (Exception exception) {
            log.error(
                    "Failed to write documents to vector store. documentCount={}",
                    documents.size(),
                    exception);

            throw new KnowledgeBaseException(
                    ErrorCode.KNOWLEDGE_BASE_VECTOR_WRITE_ERROR,
                    "知识库向量写入失败",
                    exception);
        }
    }

    public List<Document> search(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("搜索问题不能为空");
        }

        try {
            log.info("Executing similarity search.");

            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(retrievalProperties.topK())
                    .similarityThreshold(
                            retrievalProperties.similarityThreshold())
                    .build();

            List<Document> documents = vectorStore.similaritySearch(request);

            log.info(
                    "Similarity search completed. matchedDocuments={}",
                    documents.size());

            return documents;
        } catch (KnowledgeBaseException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Vector similarity search failed.", exception);

            throw new KnowledgeBaseException(
                    ErrorCode.KNOWLEDGE_BASE_VECTOR_SEARCH_ERROR,
                    "知识库检索失败",
                    exception);
        }
    }
}