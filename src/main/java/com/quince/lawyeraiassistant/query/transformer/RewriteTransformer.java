package com.quince.lawyeraiassistant.query.transformer;

import com.quince.lawyeraiassistant.query.model.QueryContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Query Rewrite 转换器。
 *
 * <p>当前为 Dummy Version，只把原始问题复制到 rewriteQuery，
 * 用于验证 Query Pipeline 的基本架构。</p>
 */
@Component
@Order(100)
public class RewriteTransformer
        implements QueryTransformer {

    @Override
    public QueryContext transform(
            QueryContext context
    ) {
        Objects.requireNonNull(
                context,
                "QueryContext must not be null"
        );

        return context.toBuilder()
                .rewriteQuery(
                        context.getQuestion()
                )
                .build();
    }
}