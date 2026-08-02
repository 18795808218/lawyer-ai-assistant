package com.quince.lawyeraiassistant.prompt.template;

import com.quince.lawyeraiassistant.prompt.knowledge.DefaultKnowledgeFormatter;
import com.quince.lawyeraiassistant.prompt.knowledge.KnowledgeFormatter;
import com.quince.lawyeraiassistant.prompt.model.PromptContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateVariablesTest {

    private KnowledgeFormatter knowledgeFormatter;

    @BeforeEach
    void setUp() {
        knowledgeFormatter = new DefaultKnowledgeFormatter();
    }

    @Test
    void shouldCreateVariablesFromPromptContext() {
        Document document = Document.builder()
                .text(
                        "第四十七条规定了经济补偿的计算标准。")
                .metadata(
                        Map.of(
                                "file_name",
                                "劳动合同法.pdf",
                                "page_number",
                                14,
                                "chunk_index",
                                0))
                .score(0.88)
                .build();

        PromptContext context = PromptContext.builder()
                .question(
                        "经济补偿金如何计算？")
                .conversationId(
                        "conversation-001")
                .knowledge(
                        List.of(document))
                .build();

        TemplateVariables variables = TemplateVariables.from(
                context,
                knowledgeFormatter);

        Map<String, Object> result = variables.toMap();

        assertEquals(
                "经济补偿金如何计算？",
                result.get(
                        TemplateVariables.QUESTION));

        assertEquals(
                "conversation-001",
                result.get(
                        TemplateVariables.CONVERSATION_ID));

        String knowledge = result.get(
                TemplateVariables.KNOWLEDGE).toString();

        assertTrue(
                knowledge.contains(
                        "## 参考资料 1"));

        assertTrue(
                knowledge.contains(
                        "劳动合同法.pdf"));

        assertTrue(
                knowledge.contains(
                        "第四十七条规定了经济补偿的计算标准"));
    }

    @Test
    void shouldUseEmptyKnowledgeWhenContextHasNoDocuments() {
        PromptContext context = PromptContext.builder()
                .question(
                        "劳动合同解除需要赔偿吗？")
                .build();

        TemplateVariables variables = TemplateVariables.from(
                context,
                knowledgeFormatter);

        Map<String, Object> result = variables.toMap();

        assertEquals(
                "",
                result.get(
                        TemplateVariables.KNOWLEDGE));
    }

    @Test
    void shouldIncludeAdditionalVariablesFromPromptContext() {
        PromptContext context = PromptContext.builder()
                .question(
                        "试用期被辞退怎么办？")
                .variable(
                        "legalDomain",
                        "劳动法")
                .variable(
                        "language",
                        "zh-CN")
                .build();

        TemplateVariables variables = TemplateVariables.from(
                context,
                knowledgeFormatter);

        Map<String, Object> result = variables.toMap();

        assertEquals(
                "劳动法",
                result.get("legalDomain"));

        assertEquals(
                "zh-CN",
                result.get("language"));
    }

    @Test
    void shouldNotAllowAdditionalVariablesToOverrideCoreVariables() {
        PromptContext context = PromptContext.builder()
                .question("原始问题")
                .variable(
                        TemplateVariables.QUESTION,
                        "尝试覆盖的问题")
                .variable(
                        TemplateVariables.KNOWLEDGE,
                        "伪造的知识")
                .build();

        TemplateVariables variables = TemplateVariables.from(
                context,
                knowledgeFormatter);

        Map<String, Object> result = variables.toMap();

        assertEquals(
                "原始问题",
                result.get(
                        TemplateVariables.QUESTION));

        assertEquals(
                "",
                result.get(
                        TemplateVariables.KNOWLEDGE));
    }

    @Test
    void shouldAlwaysIncludeCoreVariables() {
        TemplateVariables variables = TemplateVariables.builder()
                .build();

        Map<String, Object> result = variables.toMap();

        assertTrue(
                result.containsKey(
                        TemplateVariables.QUESTION));

        assertTrue(
                result.containsKey(
                        TemplateVariables.KNOWLEDGE));

        assertTrue(
                result.containsKey(
                        TemplateVariables.CONVERSATION_ID));

        assertEquals(
                "",
                result.get(
                        TemplateVariables.QUESTION));

        assertEquals(
                "",
                result.get(
                        TemplateVariables.KNOWLEDGE));

        assertEquals(
                "",
                result.get(
                        TemplateVariables.CONVERSATION_ID));
    }

    @Test
    void shouldIgnoreNullAdditionalVariableValue() {
        Map<String, Object> additionalVariables = new HashMap<>();

        additionalVariables.put(
                "language",
                "zh-CN");

        additionalVariables.put(
                "caseType",
                null);

        TemplateVariables variables = TemplateVariables.builder()
                .question(
                        "加班工资如何计算？")
                .additionalVariables(
                        additionalVariables)
                .build();

        Map<String, Object> result = variables.toMap();

        assertEquals(
                "zh-CN",
                result.get("language"));

        assertFalse(
                result.containsKey("caseType"));
    }

    @Test
    void shouldReturnUnmodifiableMap() {
        TemplateVariables variables = TemplateVariables.builder()
                .question(
                        "经济补偿如何计算？")
                .build();

        Map<String, Object> result = variables.toMap();

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.put(
                        "language",
                        "zh-CN"));
    }

    @Test
    void shouldThrowExceptionWhenPromptContextIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> TemplateVariables.from(
                        null,
                        knowledgeFormatter));

        assertEquals(
                "PromptContext must not be null",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenKnowledgeFormatterIsNull() {
        PromptContext context = PromptContext.builder()
                .question(
                        "劳动合同问题")
                .build();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> TemplateVariables.from(
                        context,
                        null));

        assertEquals(
                "KnowledgeFormatter must not be null",
                exception.getMessage());
    }
}