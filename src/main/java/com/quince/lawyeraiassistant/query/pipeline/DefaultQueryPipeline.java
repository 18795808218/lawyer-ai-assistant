package com.quince.lawyeraiassistant.query.pipeline;

import com.quince.lawyeraiassistant.query.model.QueryContext;
import com.quince.lawyeraiassistant.query.transformer.QueryTransformer;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * QueryPipeline 的默认实现。
 *
 * <p>
 * Spring 会自动注入所有 QueryTransformer Bean，
 * Pipeline 按照 @Order 或 Ordered 定义的顺序依次执行。
 * </p>
 *
 * <pre>
 * QueryContext
 *      ↓
 * RewriteTransformer
 *      ↓
 * ExpansionTransformer
 *      ↓
 * MultiQueryTransformer
 *      ↓
 * 最终 QueryContext
 * </pre>
 */
@Component
public class DefaultQueryPipeline
        implements QueryPipeline {

    private final List<QueryTransformer> transformers;

    public DefaultQueryPipeline(
            List<QueryTransformer> transformers) {
        Objects.requireNonNull(
                transformers,
                "QueryTransformer list must not be null");

        List<QueryTransformer> sortedTransformers = new ArrayList<>(transformers);

        validateTransformers(sortedTransformers);

        /*
         * 根据 @Order 或 Ordered 排序。
         *
         * 数字越小，越先执行。
         */
        AnnotationAwareOrderComparator.sort(
                sortedTransformers);

        /*
         * 保存不可修改快照，避免外部代码在运行期间
         * 动态改变 Pipeline 节点。
         */
        this.transformers = List.copyOf(sortedTransformers);
    }

    /**
     * 按顺序执行所有 QueryTransformer。
     *
     * @param context 初始查询上下文
     * @return 最终查询上下文
     */
    @Override
    public QueryContext execute(
            QueryContext context) {
        Objects.requireNonNull(
                context,
                "QueryContext must not be null");

        QueryContext currentContext = context;

        for (QueryTransformer transformer : transformers) {

            currentContext = transformer.transform(
                    currentContext);

            if (currentContext == null) {
                throw new IllegalStateException(
                        "QueryTransformer must not return null: "
                                + transformer
                                        .getClass()
                                        .getName());
            }
        }

        return currentContext;
    }

    /**
     * 校验 Pipeline 节点列表。
     */
    private void validateTransformers(
            List<QueryTransformer> transformers) {
        for (QueryTransformer transformer : transformers) {

            if (transformer == null) {
                throw new IllegalArgumentException(
                        "QueryTransformer list must not contain null");
            }
        }
    }
}