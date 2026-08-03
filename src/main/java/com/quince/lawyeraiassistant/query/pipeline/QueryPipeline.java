package com.quince.lawyeraiassistant.query.pipeline;

import com.quince.lawyeraiassistant.query.model.QueryContext;

/**
 * Query Pipeline 的统一执行入口。
 *
 * <p>负责将 QueryContext 依次交给多个 QueryTransformer，
 * 并返回全部转换完成后的上下文。</p>
 */
@FunctionalInterface
public interface QueryPipeline {

    /**
     * 执行 Query Pipeline。
     *
     * @param context 初始 QueryContext
     * @return 全部 Transformer 执行完成后的 QueryContext
     */
    QueryContext execute(QueryContext context);
}