package com.quince.lawyeraiassistant.prompt.knowledge;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 表示一个准备注入 Prompt 的知识块。
 *
 * <p>
 * KnowledgeBlock 是 Document 到 Prompt 文本之间的中间模型。
 * 它不依赖具体的模板引擎，也不负责最终 Prompt 构建。
 * </p>
 */
@Getter
@Builder
@ToString
public class KnowledgeBlock {

    /**
     * 当前知识块在最终知识上下文中的顺序，从 1 开始。
     */
    private final int index;

    /**
     * 知识来源，例如 PDF 文件名、法规名称或知识库名称。
     */
    private final String source;

    /**
     * 原始文档页码。
     */
    private final Integer pageNumber;

    /**
     * 当前 Chunk 在父文档中的序号。
     */
    private final Integer chunkIndex;

    /**
     * 检索相关度分数。
     */
    private final Double score;

    /**
     * 知识正文。
     */
    private final String content;
}