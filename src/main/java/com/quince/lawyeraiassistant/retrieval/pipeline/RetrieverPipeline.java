package com.quince.lawyeraiassistant.retrieval.pipeline;

import com.quince.lawyeraiassistant.retrieval.model.RetrieverContext;

/**
 * Retriever Pipeline 的统一执行入口。
 *
 * <p>
 * 负责将 RetrieverContext 依次交给多个 RetrievalOperator，
 * 并返回所有检索操作执行完成后的上下文。
 * </p>
 */
@FunctionalInterface
public interface RetrieverPipeline {

    /**
     * 执行 Retriever Pipeline。
     *
     * @param context 初始 RetrieverContext
     * @return 全部 RetrievalOperator 执行完成后的 RetrieverContext
     */
    RetrieverContext retrieve(
            RetrieverContext context);
}