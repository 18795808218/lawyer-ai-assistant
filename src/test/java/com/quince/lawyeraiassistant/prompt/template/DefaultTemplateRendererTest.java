package com.quince.lawyeraiassistant.prompt.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultTemplateRendererTest {

    private TemplateRenderer templateRenderer;

    @BeforeEach
    void setUp() {
        templateRenderer = new DefaultTemplateRenderer();
    }

    @Test
    void shouldRenderSingleVariable() {
        String template = """
                用户问题：
                {question}
                """;

        String result = templateRenderer.render(
                template,
                Map.of(
                        "question",
                        "劳动合同到期不续签是否有补偿？"));

        assertEquals(
                normalizeLineEndings(
                        """
                                用户问题：
                                劳动合同到期不续签是否有补偿？
                                """.trim()),
                normalizeLineEndings(result.trim()));

        assertFalse(
                result.contains("{question}"));
    }

    @Test
    void shouldRenderMultipleVariables() {
        String template = """
                会话编号：{conversationId}
                参考知识：{knowledge}
                用户问题：{question}
                """;

        String result = templateRenderer.render(
                template,
                Map.of(
                        "conversationId",
                        "conversation-001",
                        "knowledge",
                        "违法解除劳动合同可能需要支付赔偿金。",
                        "question",
                        "公司违法辞退我怎么办？"));

        assertEquals(
                normalizeLineEndings(
                        """
                                会话编号：conversation-001
                                参考知识：违法解除劳动合同可能需要支付赔偿金。
                                用户问题：公司违法辞退我怎么办？
                                """.trim()),
                normalizeLineEndings(result.trim()));

        assertFalse(
                result.contains("{conversationId}"));

        assertFalse(
                result.contains("{knowledge}"));

        assertFalse(
                result.contains("{question}"));
    }

    @Test
    void shouldRenderStaticTemplateWithEmptyVariables() {
        String template = "你是一名专业的中国法律助手。";

        String result = templateRenderer.render(
                template,
                Map.of());

        assertEquals(template, result);
    }

    @Test
    void shouldRenderStaticTemplateWhenVariablesAreNull() {
        String template = "你是一名专业的中国法律助手。";

        String result = templateRenderer.render(
                template,
                null);

        assertEquals(template, result);
    }

    @Test
    void shouldRenderNonStringVariable() {
        String template = "检索文档数量：{documentCount}";

        String result = templateRenderer.render(
                template,
                Map.of(
                        "documentCount",
                        5));

        assertEquals(
                "检索文档数量：5",
                result);
    }

    @Test
    void shouldThrowExceptionWhenTemplateIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> templateRenderer.render(
                        null,
                        Map.of()));

        assertEquals(
                "Prompt template must not be null",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTemplateIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateRenderer.render(
                        "   ",
                        Map.of()));

        assertEquals(
                "Prompt template must not be blank",
                exception.getMessage());
    }

    private String normalizeLineEndings(String value) {
        return value.replace(
                "\r\n",
                "\n");
    }
}
