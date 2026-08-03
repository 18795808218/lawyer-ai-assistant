package com.quince.lawyeraiassistant.query.transformer;

import com.quince.lawyeraiassistant.query.model.QueryContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewriteTransformerTest {

    private QueryTransformer rewriteTransformer;

    @BeforeEach
    void setUp() {
        rewriteTransformer =
                new RewriteTransformer();
    }

    @Test
    void shouldCopyOriginalQuestionToRewriteQuery() {
        QueryContext context =
                QueryContext.from(
                        "老板把我开了合法吗？"
                );

        QueryContext result =
                rewriteTransformer.transform(context);

        assertEquals(
                "老板把我开了合法吗？",
                result.getRewriteQuery()
        );

        assertTrue(result.hasRewriteQuery());
    }

    @Test
    void shouldPreserveOriginalQuestion() {
        QueryContext context =
                QueryContext.from(
                        "公司不给我交社保。"
                );

        QueryContext result =
                rewriteTransformer.transform(context);

        assertEquals(
                "公司不给我交社保。",
                result.getQuestion()
        );

        assertEquals(
                "公司不给我交社保。",
                result.getRewriteQuery()
        );
    }

    @Test
    void shouldReturnRewriteQueryAsEffectiveQuery() {
        QueryContext context =
                QueryContext.from(
                        "老板不给工资怎么办？"
                );

        QueryContext result =
                rewriteTransformer.transform(context);

        assertEquals(
                "老板不给工资怎么办？",
                result.effectiveQuery()
        );

        assertTrue(result.hasRewriteQuery());
    }

    @Test
    void shouldPreserveConversationId() {
        QueryContext context =
                QueryContext.from(
                        "那赔偿呢？",
                        "conversation-001"
                );

        QueryContext result =
                rewriteTransformer.transform(context);

        assertEquals(
                "conversation-001",
                result.getConversationId()
        );

        assertTrue(
                result.hasConversationId()
        );
    }

    @Test
    void shouldReturnNewContextWithoutModifyingOriginal() {
        QueryContext originalContext =
                QueryContext.from(
                        "劳动合同到期怎么办？"
                );

        QueryContext transformedContext =
                rewriteTransformer.transform(
                        originalContext
                );

        assertNotSame(
                originalContext,
                transformedContext
        );

        assertNull(
                originalContext.getRewriteQuery()
        );

        assertEquals(
                "劳动合同到期怎么办？",
                originalContext.effectiveQuery()
        );

        assertEquals(
                "劳动合同到期怎么办？",
                transformedContext.getRewriteQuery()
        );
    }

    @Test
    void shouldOverwriteExistingRewriteQueryUsingOriginalQuestion() {
        QueryContext context =
                QueryContext.builder()
                        .question(
                                "老板把我开了合法吗？"
                        )
                        .rewriteQuery(
                                "旧的改写结果"
                        )
                        .build();

        QueryContext result =
                rewriteTransformer.transform(context);

        assertEquals(
                "老板把我开了合法吗？",
                result.getRewriteQuery()
        );
    }

    @Test
    void shouldThrowExceptionWhenContextIsNull() {
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> rewriteTransformer.transform(
                                null
                        )
                );

        assertEquals(
                "QueryContext must not be null",
                exception.getMessage()
        );
    }
}