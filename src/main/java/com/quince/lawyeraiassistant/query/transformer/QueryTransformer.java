package com.quince.lawyeraiassistant.query.transformer;

import com.quince.lawyeraiassistant.query.model.QueryContext;

/**
 * Query Pipeline 中的转换节点。
 *
 * <p>每个 Transformer 负责完成 Query 的一次转换，
 * 并返回新的 QueryContext。</p>
 *
 * <p>例如：</p>
 *
 * <ul>
 *     <li>RewriteTransformer</li>
 *     <li>ExpansionTransformer</li>
 *     <li>MultiQueryTransformer</li>
 *     <li>HyDETransformer（未来）</li>
 * </ul>
 */
@FunctionalInterface
public interface QueryTransformer {

    /**
     * 执行 Query 转换。
     *
     * @param context 当前 QueryContext
     * @return 转换后的 QueryContext
     */
    QueryContext transform(QueryContext context);

}