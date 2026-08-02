package com.quince.lawyeraiassistant.prompt.knowledge;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 将 Spring AI Document 格式化为可注入 Prompt 的知识文本。
 */
public interface KnowledgeFormatter {

    /**
     * 格式化检索文档。
     *
     * @param documents RAG 检索返回的文档
     * @return 适合注入 Prompt 的结构化文本；
     *         没有有效文档时返回空字符串
     */
    String format(List<Document> documents);
}