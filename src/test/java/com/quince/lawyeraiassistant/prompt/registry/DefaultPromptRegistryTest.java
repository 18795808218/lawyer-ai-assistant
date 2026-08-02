package com.quince.lawyeraiassistant.prompt.registry;

import com.quince.lawyeraiassistant.prompt.model.PromptFragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPromptRegistryTest {

        private PromptRegistry promptRegistry;

        @BeforeEach
        void setUp() {
                promptRegistry = new DefaultPromptRegistry();
        }

        @Test
        void shouldRegisterPromptSuccessfully() {
                PromptFragment fragment = createFragment(
                                "lawyer-system",
                                "你是一名专业律师。");

                promptRegistry.register(fragment);

                assertTrue(
                                promptRegistry.contains(
                                                "lawyer-system"));
        }

        @Test
        void shouldFindRegisteredPrompt() {
                PromptFragment fragment = createFragment(
                                "lawyer-system",
                                "你是一名专业律师。");

                promptRegistry.register(fragment);

                PromptFragment result = promptRegistry.find(
                                "lawyer-system");

                assertSame(fragment, result);

                assertEquals(
                                "你是一名专业律师。",
                                result.getContent());
        }

        @Test
        void shouldThrowExceptionWhenRegisteringDuplicatePrompt() {
                PromptFragment firstFragment = createFragment(
                                "lawyer-system",
                                "第一份 Prompt");

                PromptFragment duplicateFragment = createFragment(
                                "lawyer-system",
                                "第二份 Prompt");

                promptRegistry.register(firstFragment);

                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> promptRegistry.register(
                                                duplicateFragment));

                assertEquals(
                                "Prompt already registered: lawyer-system",
                                exception.getMessage());
        }

        @Test
        void shouldKeepOriginalPromptWhenDuplicateRegistrationFails() {
                PromptFragment originalFragment = createFragment(
                                "lawyer-system",
                                "原始 Prompt");

                PromptFragment duplicateFragment = createFragment(
                                "lawyer-system",
                                "重复 Prompt");

                promptRegistry.register(
                                originalFragment);

                assertThrows(
                                IllegalStateException.class,
                                () -> promptRegistry.register(
                                                duplicateFragment));

                PromptFragment result = promptRegistry.find(
                                "lawyer-system");

                assertSame(
                                originalFragment,
                                result);

                assertEquals(
                                "原始 Prompt",
                                result.getContent());
        }

        @Test
        void shouldThrowExceptionWhenPromptDoesNotExist() {
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> promptRegistry.find(
                                                "not-exist"));

                assertEquals(
                                "Prompt not found: not-exist",
                                exception.getMessage());
        }

        @Test
        void shouldReturnFalseWhenPromptDoesNotExist() {
                assertFalse(
                                promptRegistry.contains(
                                                "not-exist"));
        }

        @Test
        void shouldClearRegisteredPrompts() {
                promptRegistry.register(
                                createFragment(
                                                "lawyer-system",
                                                "律师系统 Prompt"));

                promptRegistry.register(
                                createFragment(
                                                "citation-rules",
                                                "引用规则"));

                promptRegistry.clear();

                assertFalse(
                                promptRegistry.contains(
                                                "lawyer-system"));

                assertFalse(
                                promptRegistry.contains(
                                                "citation-rules"));
        }

        @Test
        void shouldThrowExceptionWhenRegisteringNullPrompt() {
                NullPointerException exception = assertThrows(
                                NullPointerException.class,
                                () -> promptRegistry.register(null));

                assertEquals(
                                "Prompt fragment must not be null",
                                exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenPromptNameIsNull() {
                PromptFragment fragment = PromptFragment.builder()
                                .name(null)
                                .content("Prompt content")
                                .version("v1")
                                .source("classpath:test.txt")
                                .build();

                NullPointerException exception = assertThrows(
                                NullPointerException.class,
                                () -> promptRegistry.register(
                                                fragment));

                assertEquals(
                                "Prompt name must not be null",
                                exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenPromptNameIsBlank() {
                PromptFragment fragment = PromptFragment.builder()
                                .name("   ")
                                .content("Prompt content")
                                .version("v1")
                                .source("classpath:test.txt")
                                .build();

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> promptRegistry.register(
                                                fragment));

                assertEquals(
                                "Prompt name must not be blank",
                                exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenFindingNullName() {
                NullPointerException exception = assertThrows(
                                NullPointerException.class,
                                () -> promptRegistry.find(null));

                assertEquals(
                                "Prompt name must not be null",
                                exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenFindingBlankName() {
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> promptRegistry.find("   "));

                assertEquals(
                                "Prompt name must not be blank",
                                exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenContainsNameIsNull() {
                NullPointerException exception = assertThrows(
                                NullPointerException.class,
                                () -> promptRegistry.contains(null));

                assertEquals(
                                "Prompt name must not be null",
                                exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenContainsNameIsBlank() {
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> promptRegistry.contains("   "));

                assertEquals(
                                "Prompt name must not be blank",
                                exception.getMessage());
        }

        private PromptFragment createFragment(
                        String name,
                        String content) {
                return PromptFragment.builder()
                                .name(name)
                                .content(content)
                                .version("v1")
                                .source(
                                                "classpath:prompts/"
                                                                + name
                                                                + ".txt")
                                .build();
        }
}