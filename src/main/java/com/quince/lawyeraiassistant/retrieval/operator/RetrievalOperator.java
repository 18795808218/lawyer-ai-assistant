package com.quince.lawyeraiassistant.retrieval.operator;

import com.quince.lawyeraiassistant.retrieval.model.RetrieverContext;

/**
 * Retriever Pipeline 中的操作节点。
 *
 * <p>
 * 每个 Operator 完成 Retriever Pipeline 的一个步骤，
 * 并返回新的 RetrieverContext。
 * </p>
 *
 * <p>
 * 例如：
 * </p>
 *
 * <ul>
 * <li>VectorSearchOperator</li>
 * <li>ParentRetrievalOperator</li>
 * <li>MergeOperator</li>
 * <li>HybridSearchOperator</li>
 * <li>ReRankOperator</li>
 * </ul>
 */
@FunctionalInterface
public interface RetrievalOperator {

    /**
     * 执行 Retriever Pipeline 的一个操作。
     *
     * @param context 当前 RetrieverContext
     * @return 更新后的 RetrieverContext
     */
    RetrieverContext retrieve(
            RetrieverContext context);

}