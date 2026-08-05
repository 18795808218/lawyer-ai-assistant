package com.quince.lawyeraiassistant.retrieval.pipeline;

import com.quince.lawyeraiassistant.query.model.QueryContext;
import com.quince.lawyeraiassistant.retrieval.model.RetrieverContext;
import com.quince.lawyeraiassistant.retrieval.operator.RetrievalOperator;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRetrieverPipelineTest {

    @Test
    void shouldExecuteOperatorsInOriginalOrderWhenOrderIsEqual() {
        List<String> executionOrder = new ArrayList<>();

        RetrievalOperator firstOperator = context -> {
            executionOrder.add("first");

            return context.toBuilder()
                    .documents(
                            List.of(
                                    new Document(
                                            "第一次检索结果")))
                    .build();
        };

        RetrievalOperator secondOperator = context -> {
            executionOrder.add("second");

            List<Document> documents = new ArrayList<>(
                    context.getDocuments());

            documents.add(
                    new Document(
                            "第二次检索结果"));

            return context.toBuilder()
                    .documents(documents)
                    .build();
        };

        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of(
                        firstOperator,
                        secondOperator));

        RetrieverContext result = pipeline.retrieve(
                createContext(
                        "劳动合同问题"));

        assertEquals(
                List.of("first", "second"),
                executionOrder);

        assertEquals(
                2,
                result.documentCount());

        assertEquals(
                "第一次检索结果",
                result.getDocuments()
                        .get(0)
                        .getText());

        assertEquals(
                "第二次检索结果",
                result.getDocuments()
                        .get(1)
                        .getText());
    }

    @Test
    void shouldPassPreviousResultToNextOperator() {
        RetrievalOperator vectorOperator = context -> context.toBuilder()
                .documents(
                        List.of(
                                new Document(
                                        "劳动合同法第四十六条")))
                .build();

        RetrievalOperator parentOperator = context -> {
            String childText = context.getDocuments()
                    .get(0)
                    .getText();

            return context.toBuilder()
                    .documents(
                            List.of(
                                    new Document(
                                            "父文档："
                                                    + childText
                                                    + "及相关条款")))
                    .build();
        };

        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of(
                        vectorOperator,
                        parentOperator));

        RetrieverContext result = pipeline.retrieve(
                createContext(
                        "解除劳动合同赔偿"));

        assertEquals(
                1,
                result.documentCount());

        assertEquals(
                "父文档：劳动合同法第四十六条及相关条款",
                result.getDocuments()
                        .get(0)
                        .getText());
    }

    @Test
    void shouldPreserveOriginalContextImmutability() {
        RetrieverContext originalContext = createContext(
                "竞业协议合法吗？");

        RetrievalOperator operator = context -> context.toBuilder()
                .documents(
                        List.of(
                                new Document(
                                        "竞业限制相关规定")))
                .build();

        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of(operator));

        RetrieverContext result = pipeline.retrieve(
                originalContext);

        assertNotSame(
                originalContext,
                result);

        assertEquals(
                0,
                originalContext.documentCount());

        assertEquals(
                1,
                result.documentCount());

        assertEquals(
                originalContext.getQueryContext(),
                result.getQueryContext());
    }

    @Test
    void shouldReturnOriginalContextWhenPipelineIsEmpty() {
        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of());

        RetrieverContext context = createContext(
                "劳动合同到期怎么办？");

        RetrieverContext result = pipeline.retrieve(context);

        assertSame(context, result);
    }

    @Test
    void shouldSortOperatorsByOrderAnnotation() {
        List<String> executionOrder = new ArrayList<>();

        RetrievalOperator laterOperator = new LaterOperator(
                executionOrder);

        RetrievalOperator earlierOperator = new EarlierOperator(
                executionOrder);

        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of(
                        laterOperator,
                        earlierOperator));

        pipeline.retrieve(
                createContext(
                        "测试问题"));

        assertEquals(
                List.of("earlier", "later"),
                executionOrder);
    }

    @Test
    void shouldThrowExceptionWhenContextIsNull() {
        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of());

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> pipeline.retrieve(null));

        assertEquals(
                "RetrieverContext must not be null",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOperatorListIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new DefaultRetrieverPipeline(
                        null));

        assertEquals(
                "RetrievalOperator list must not be null",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOperatorListContainsNull() {
        List<RetrievalOperator> operators = Arrays.asList(
                context -> context,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new DefaultRetrieverPipeline(
                        operators));

        assertEquals(
                "RetrievalOperator list must not contain null",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOperatorReturnsNull() {
        RetrievalOperator invalidOperator = context -> null;

        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of(
                        invalidOperator));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> pipeline.retrieve(
                        createContext(
                                "测试问题")));

        assertTrue(
                exception.getMessage()
                        .startsWith(
                                "RetrievalOperator must not return null:"));
    }

    @Test
    void shouldPropagateOperatorException() {
        IllegalStateException expectedException = new IllegalStateException(
                "Vector search failed");

        RetrievalOperator operator = context -> {
            throw expectedException;
        };

        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of(operator));

        IllegalStateException actualException = assertThrows(
                IllegalStateException.class,
                () -> pipeline.retrieve(
                        createContext(
                                "测试问题")));

        assertSame(
                expectedException,
                actualException);
    }

    @Test
    void shouldPreserveEffectiveQueryDuringRetrieval() {
        QueryContext queryContext = QueryContext.builder()
                .question(
                        "老板把我开了合法吗？")
                .rewriteQuery(
                        "违法解除劳动合同是否合法")
                .build();

        RetrieverContext context = RetrieverContext.from(
                queryContext);

        RetrievalOperator operator = currentContext -> {
            assertEquals(
                    "违法解除劳动合同是否合法",
                    currentContext.effectiveQuery());

            return currentContext;
        };

        RetrieverPipeline pipeline = new DefaultRetrieverPipeline(
                List.of(operator));

        RetrieverContext result = pipeline.retrieve(context);

        assertEquals(
                "违法解除劳动合同是否合法",
                result.effectiveQuery());

        assertEquals(
                "老板把我开了合法吗？",
                result.getQueryContext()
                        .getQuestion());
    }

    private RetrieverContext createContext(
            String question) {
        return RetrieverContext.from(
                QueryContext.from(question));
    }

    @Order(100)
    private static class EarlierOperator
            implements RetrievalOperator {

        private final List<String> executionOrder;

        private EarlierOperator(
                List<String> executionOrder) {
            this.executionOrder = executionOrder;
        }

        @Override
        public RetrieverContext retrieve(
                RetrieverContext context) {
            executionOrder.add("earlier");
            return context;
        }
    }

    @Order(200)
    private static class LaterOperator
            implements RetrievalOperator {

        private final List<String> executionOrder;

        private LaterOperator(
                List<String> executionOrder) {
            this.executionOrder = executionOrder;
        }

        @Override
        public RetrieverContext retrieve(
                RetrieverContext context) {
            executionOrder.add("later");
            return context;
        }
    }
}