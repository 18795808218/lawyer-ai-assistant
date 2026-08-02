package com.quince.lawyeraiassistant.prompt.loader;

import com.quince.lawyeraiassistant.prompt.model.PromptFragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.junit.jupiter.api.Assertions.*;

class ClasspathPromptLoaderTest {

    private PromptLoader promptLoader;

    @BeforeEach
    void setUp() {

        ResourceLoader resourceLoader = new DefaultResourceLoader();

        promptLoader = new ClasspathPromptLoader(resourceLoader);

    }

    @Test
    void shouldLoadPromptSuccessfully() {

        PromptFragment fragment = promptLoader.load(
                "classpath:prompts/system/lawyer-system.st");

        assertNotNull(fragment);

        assertEquals("lawyer-system", fragment.getName());

        assertEquals("v1", fragment.getVersion());

        assertEquals(
                "classpath:prompts/system/lawyer-system.st",
                fragment.getSource());

        assertNotNull(fragment.getContent());

        assertFalse(fragment.getContent().isBlank());

    }

    @Test
    void shouldExtractPromptNameCorrectly() {

        PromptFragment fragment = promptLoader.load(
                "classpath:prompts/legal/case-analysis.st");

        assertEquals("case-analysis", fragment.getName());

    }

    @Test
    void shouldThrowExceptionWhenPromptDoesNotExist() {

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> promptLoader.load(
                        "classpath:prompts/not-exist.st"));

        assertTrue(exception.getMessage().contains("Prompt"));

    }

    @Test
    void shouldKeepOriginalSourceLocation() {

        String location = "classpath:prompts/system/lawyer-system.st";

        PromptFragment fragment = promptLoader.load(location);

        assertEquals(location, fragment.getSource());

    }

}