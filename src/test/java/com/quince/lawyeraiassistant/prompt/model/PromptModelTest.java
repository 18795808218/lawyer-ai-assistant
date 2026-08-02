package com.quince.lawyeraiassistant.prompt.model;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptModelTest {

    @Test
    void shouldCreatePromptFragment() {
        PromptFragment fragment = PromptFragment.builder()
                .name("lawyer-identity")
                .content("你是一名专业律师。")
                .version("v1")
                .source("classpath:prompts/v1/identity/lawyer.md")
                .build();

        assertEquals("lawyer-identity", fragment.getName());
        assertEquals("你是一名专业律师。", fragment.getContent());
        assertEquals("v1", fragment.getVersion());
        assertEquals(
                "classpath:prompts/v1/identity/lawyer.md",
                fragment.getSource());
    }

    @Test
    void shouldCreatePromptContextWithoutKnowledge() {
        PromptContext context = PromptContext.builder()
                .question("劳动合同到期不续签需要赔偿吗？")
                .conversationId("conversation-001")
                .variable("legalDomain", "劳动法")
                .build();

        assertEquals(
                "劳动合同到期不续签需要赔偿吗？",
                context.getQuestion());
        assertEquals("conversation-001", context.getConversationId());
        assertEquals("劳动法", context.safeVariables().get("legalDomain"));

        assertTrue(context.safeKnowledge().isEmpty());
        assertTrue(context.hasConversationId());
        assertFalse(context.hasKnowledge());
    }

    @Test
    void shouldCreatePromptContextWithKnowledge() {
        Document document = new Document(
                "劳动合同期满，公司决定不续签的，"
                        + "在符合法律规定的情况下可能需要支付经济补偿。");

        PromptContext context = PromptContext.builder()
                .question("合同到期不续签是否需要补偿？")
                .knowledge(List.of(document))
                .build();

        assertTrue(context.hasKnowledge());
        assertFalse(context.hasConversationId());
        assertEquals(1, context.safeKnowledge().size());
        assertEquals(
                document,
                context.safeKnowledge().getFirst());
    }
}