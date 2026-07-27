package com.quince.lawyeraiassistant.rag.pipeline;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 文档预处理流水线。
 *
 * 负责把原始文档转换为可以进入 Embedding 阶段的 Chunk。
 */
public interface DocumentPipeline {

    /**
     * 执行完整文档处理流程。
     *
     * @return 处理完成后的文档块
     */
    List<Document> process();
}