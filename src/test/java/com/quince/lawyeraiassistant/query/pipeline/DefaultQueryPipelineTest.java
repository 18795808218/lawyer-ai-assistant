package com.quince.lawyeraiassistant.query.pipeline;

import com.quince.lawyeraiassistant.query.model.QueryContext;
import com.quince.lawyeraiassistant.query.transformer.QueryTransformer;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultQueryPipelineTest {

    @Test
    void shouldExecuteTransformersInOrder() {
        List<String> executionOrder =
                new ArrayList<>();

        QueryTransformer firstTransformer =
                context -> {
                    executionOrder.add("first");

                    return context.toBuilder()
                            .rewriteQuery(
                                    "第一次转换"
                            )
                            .build();
                };

        QueryTransformer secondTransformer =
                context -> {
                    executionOrder.add("second");

                    return context.toBuilder()
                            .rewriteQuery(
                                    context.getRewriteQuery()
                                            + " -> 第二次转换"
                            )
                            .build();
                };

        QueryPipeline pipeline =
                new DefaultQueryPipeline(
                        List.of(
                                firstTransformer,
                                secondTransformer
                        )
                );

        QueryContext result =
                pipeline.execute(
                        QueryContext.from(
                                "原始问题"
                        )
                );

        assertEquals(
                List.of("first", "second"),
                executionOrder
        );

        assertEquals(
                "第一次转换 -> 第二次转换",
                result.getRewriteQuery()
        );
    }

    @Test
    void shouldPassPreviousResultToNextTransformer() {
        QueryTransformer rewriteTransformer =
                context ->
                        context.toBuilder()
                                .rewriteQuery(
                                        "违法解除劳动合同"
                                )
                                .build();

        QueryTransformer suffixTransformer =
                context ->
                        context.toBuilder()
                                .rewriteQuery(
                                        context.effectiveQuery()
                                                + " 赔偿标准"
                                )
                                .build();

        QueryPipeline pipeline =
                new DefaultQueryPipeline(
                        List.of(
                                rewriteTransformer,
                                suffixTransformer
                        )
                );

        QueryContext result =
                pipeline.execute(
                        QueryContext.from(
                                "老板把我开了怎么办？"
                        )
                );

        assertEquals(
                "违法解除劳动合同 赔偿标准",
                result.effectiveQuery()
        );
    }

    @Test
    void shouldPreserveOriginalContextImmutability() {
        QueryContext originalContext =
                QueryContext.from(
                        "老板不给工资怎么办？",
                        "conversation-001"
                );

        QueryTransformer transformer =
                context ->
                        context.toBuilder()
                                .rewriteQuery(
                                        "拖欠劳动报酬的法律救济"
                                )
                                .build();

        QueryPipeline pipeline =
                new DefaultQueryPipeline(
                        List.of(transformer)
                );

        QueryContext result =
                pipeline.execute(
                        originalContext
                );

        assertNotSame(
                originalContext,
                result
        );

        assertEquals(
                "老板不给工资怎么办？",
                originalContext.effectiveQuery()
        );

        assertEquals(
                "拖欠劳动报酬的法律救济",
                result.effectiveQuery()
        );

        assertEquals(
                "conversation-001",
                result.getConversationId()
        );
    }

    @Test
    void shouldReturnOriginalContextWhenPipelineIsEmpty() {
        QueryPipeline pipeline =
                new DefaultQueryPipeline(
                        List.of()
                );

        QueryContext context =
                QueryContext.from(
                        "劳动合同到期怎么办？"
                );

        QueryContext result =
                pipeline.execute(context);

        assertSame(context, result);
    }

    @Test
    void shouldSortTransformersByOrderAnnotation() {
        List<String> executionOrder =
                new ArrayList<>();

        QueryTransformer laterTransformer =
                new LaterTransformer(
                        executionOrder
                );

        QueryTransformer earlierTransformer =
                new EarlierTransformer(
                        executionOrder
                );

        QueryPipeline pipeline =
                new DefaultQueryPipeline(
                        List.of(
                                laterTransformer,
                                earlierTransformer
                        )
                );

        pipeline.execute(
                QueryContext.from(
                        "测试问题"
                )
        );

        assertEquals(
                List.of("earlier", "later"),
                executionOrder
        );
    }

    @Test
    void shouldThrowExceptionWhenContextIsNull() {
        QueryPipeline pipeline =
                new DefaultQueryPipeline(
                        List.of()
                );

        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> pipeline.execute(null)
                );

        assertEquals(
                "QueryContext must not be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenTransformerListIsNull() {
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> new DefaultQueryPipeline(
                                null
                        )
                );

        assertEquals(
                "QueryTransformer list must not be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenTransformerListContainsNull() {
        List<QueryTransformer> transformers =
                Arrays.asList(
                        context -> context,
                        null
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new DefaultQueryPipeline(
                                transformers
                        )
                );

        assertEquals(
                "QueryTransformer list must not contain null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenTransformerReturnsNull() {
        QueryTransformer invalidTransformer =
                context -> null;

        QueryPipeline pipeline =
                new DefaultQueryPipeline(
                        List.of(
                                invalidTransformer
                        )
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> pipeline.execute(
                                QueryContext.from(
                                        "测试问题"
                                )
                        )
                );

        assertTrue(
                exception.getMessage()
                        .startsWith(
                                "QueryTransformer must not return null:"
                        )
        );
    }

    @Test
    void shouldPropagateTransformerException() {
        IllegalStateException expectedException =
                new IllegalStateException(
                        "Rewrite failed"
                );

        QueryTransformer transformer =
                context -> {
                    throw expectedException;
                };

        QueryPipeline pipeline =
                new DefaultQueryPipeline(
                        List.of(transformer)
                );

        IllegalStateException actualException =
                assertThrows(
                        IllegalStateException.class,
                        () -> pipeline.execute(
                                QueryContext.from(
                                        "测试问题"
                                )
                        )
                );

        assertSame(
                expectedException,
                actualException
        );
    }

    @Order(100)
    private static class EarlierTransformer
            implements QueryTransformer {

        private final List<String> executionOrder;

        private EarlierTransformer(
                List<String> executionOrder
        ) {
            this.executionOrder =
                    executionOrder;
        }

        @Override
        public QueryContext transform(
                QueryContext context
        ) {
            executionOrder.add("earlier");
            return context;
        }
    }

    @Order(200)
    private static class LaterTransformer
            implements QueryTransformer {

        private final List<String> executionOrder;

        private LaterTransformer(
                List<String> executionOrder
        ) {
            this.executionOrder =
                    executionOrder;
        }

        @Override
        public QueryContext transform(
                QueryContext context
        ) {
            executionOrder.add("later");
            return context;
        }
    }
}