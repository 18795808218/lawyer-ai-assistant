package com.quince.lawyeraiassistant.query.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryContextTest {

    @Test
    void shouldCreateContextFromQuestion() {
        QueryContext context =
                QueryContext.from(
                        "老板把我开了合法吗？"
                );

        assertEquals(
                "老板把我开了合法吗？",
                context.getQuestion()
        );

        assertNull(context.getConversationId());
        assertNull(context.getRewriteQuery());

        assertFalse(context.hasConversationId());
        assertFalse(context.hasRewriteQuery());
    }

    @Test
    void shouldCreateContextWithConversationId() {
        QueryContext context =
                QueryContext.from(
                        "那赔偿呢？",
                        "conversation-001"
                );

        assertEquals(
                "那赔偿呢？",
                context.getQuestion()
        );

        assertEquals(
                "conversation-001",
                context.getConversationId()
        );

        assertTrue(context.hasConversationId());
    }

    @Test
    void shouldTrimQuestionAndConversationId() {
        QueryContext context =
                QueryContext.from(
                        "  劳动合同到期怎么办？  ",
                        "  conversation-002  "
                );

        assertEquals(
                "劳动合同到期怎么办？",
                context.getQuestion()
        );

        assertEquals(
                "conversation-002",
                context.getConversationId()
        );
    }

    @Test
    void shouldNormalizeBlankConversationIdToNull() {
        QueryContext context =
                QueryContext.from(
                        "经济补偿如何计算？",
                        "   "
                );

        assertNull(context.getConversationId());
        assertFalse(context.hasConversationId());
    }

    @Test
    void shouldReturnOriginalQuestionBeforeRewrite() {
        QueryContext context =
                QueryContext.from(
                        "老板不给工资怎么办？"
                );

        assertEquals(
                "老板不给工资怎么办？",
                context.effectiveQuery()
        );
    }

    @Test
    void shouldReturnRewriteQueryAfterRewrite() {
        QueryContext originalContext =
                QueryContext.from(
                        "老板不给工资怎么办？"
                );

        QueryContext rewrittenContext =
                originalContext.toBuilder()
                        .rewriteQuery(
                                "拖欠劳动报酬的法律救济"
                        )
                        .build();

        assertEquals(
                "拖欠劳动报酬的法律救济",
                rewrittenContext.getRewriteQuery()
        );

        assertEquals(
                "拖欠劳动报酬的法律救济",
                rewrittenContext.effectiveQuery()
        );

        assertTrue(
                rewrittenContext.hasRewriteQuery()
        );
    }

    @Test
    void shouldPreserveOriginalQuestionAfterRewrite() {
        QueryContext originalContext =
                QueryContext.from(
                        "老板把我开了合法吗？"
                );

        QueryContext rewrittenContext =
                originalContext.toBuilder()
                        .rewriteQuery(
                                "违法解除劳动合同是否合法"
                        )
                        .build();

        assertEquals(
                "老板把我开了合法吗？",
                rewrittenContext.getQuestion()
        );

        assertEquals(
                "违法解除劳动合同是否合法",
                rewrittenContext.getRewriteQuery()
        );
    }

    @Test
    void shouldNotModifyOriginalContextWhenCreatingRewriteContext() {
        QueryContext originalContext =
                QueryContext.from(
                        "公司不给我交社保。"
                );

        QueryContext rewrittenContext =
                originalContext.toBuilder()
                        .rewriteQuery(
                                "用人单位未依法缴纳社会保险"
                        )
                        .build();

        assertNull(
                originalContext.getRewriteQuery()
        );

        assertEquals(
                "公司不给我交社保。",
                originalContext.effectiveQuery()
        );

        assertEquals(
                "用人单位未依法缴纳社会保险",
                rewrittenContext.effectiveQuery()
        );

        assertNotEquals(
                originalContext,
                rewrittenContext
        );
    }

    @Test
    void shouldTrimRewriteQuery() {
        QueryContext context =
                QueryContext.builder()
                        .question(
                                "公司把我辞退了。"
                        )
                        .rewriteQuery(
                                "  解除劳动合同是否合法  "
                        )
                        .build();

        assertEquals(
                "解除劳动合同是否合法",
                context.getRewriteQuery()
        );
    }

    @Test
    void shouldNormalizeBlankRewriteQueryToNull() {
        QueryContext context =
                QueryContext.builder()
                        .question(
                                "公司把我辞退了。"
                        )
                        .rewriteQuery("   ")
                        .build();

        assertNull(context.getRewriteQuery());
        assertFalse(context.hasRewriteQuery());

        assertEquals(
                "公司把我辞退了。",
                context.effectiveQuery()
        );
    }

    @Test
    void shouldThrowExceptionWhenQuestionIsNull() {
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> QueryContext.from(null)
                );

        assertEquals(
                "Query question must not be null",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenQuestionIsBlank() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> QueryContext.from("   ")
                );

        assertEquals(
                "Query question must not be blank",
                exception.getMessage()
        );
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        QueryContext first =
                QueryContext.builder()
                        .question(
                                "劳动合同解除是否合法？"
                        )
                        .conversationId(
                                "conversation-001"
                        )
                        .rewriteQuery(
                                "解除劳动合同合法性"
                        )
                        .build();

        QueryContext second =
                QueryContext.builder()
                        .question(
                                "劳动合同解除是否合法？"
                        )
                        .conversationId(
                                "conversation-001"
                        )
                        .rewriteQuery(
                                "解除劳动合同合法性"
                        )
                        .build();

        assertEquals(first, second);
        assertEquals(
                first.hashCode(),
                second.hashCode()
        );
    }
}