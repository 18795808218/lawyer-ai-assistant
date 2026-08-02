package com.quince.lawyeraiassistant.prompt;

import com.quince.lawyeraiassistant.prompt.loader.PromptLoader;
import com.quince.lawyeraiassistant.prompt.model.PromptFragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromptResourceLoaderTest {

    private ResourceLoader resourceLoader;

    private PromptLoader promptLoader;

    private PromptResourceLoader promptResourceLoader;

    @BeforeEach
    void setUp() {
        resourceLoader = mock(ResourceLoader.class);

        promptLoader = mock(PromptLoader.class);

        promptResourceLoader = new PromptResourceLoader(
                resourceLoader,
                promptLoader);
    }

    @Test
    void shouldDelegateLoadToPromptLoader() {
        String location = "classpath:prompts/test.st";

        PromptFragment fragment = PromptFragment.builder()
                .name("test")
                .content("Prompt content")
                .version("v1")
                .source(location)
                .build();

        when(
                promptLoader.load(location)).thenReturn(fragment);

        String result = promptResourceLoader.load(location);

        assertEquals(
                "Prompt content",
                result);

        verify(promptLoader).load(location);
    }

    @Test
    void shouldReturnReadableResource() {
        String location = "classpath:prompts/test.st";

        Resource resource = mock(Resource.class);

        when(
                resourceLoader.getResource(location)).thenReturn(resource);

        when(resource.exists())
                .thenReturn(true);

        when(resource.isReadable())
                .thenReturn(true);

        Resource result = promptResourceLoader.getResource(
                location);

        assertSame(resource, result);
    }

    @Test
    void shouldThrowExceptionWhenLocationIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> promptResourceLoader
                        .getResource(null));

        assertEquals(
                "Prompt resource location must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLocationIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> promptResourceLoader
                        .getResource("   "));

        assertEquals(
                "Prompt resource location must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenResourceDoesNotExist() {
        String location = "classpath:prompts/not-exist.st";

        Resource resource = mock(Resource.class);

        when(
                resourceLoader.getResource(location)).thenReturn(resource);

        when(resource.exists())
                .thenReturn(false);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> promptResourceLoader
                        .getResource(location));

        assertEquals(
                "Prompt resource does not exist: "
                        + location,
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenResourceIsNotReadable() {
        String location = "classpath:prompts/not-readable.st";

        Resource resource = mock(Resource.class);

        when(
                resourceLoader.getResource(location)).thenReturn(resource);

        when(resource.exists())
                .thenReturn(true);

        when(resource.isReadable())
                .thenReturn(false);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> promptResourceLoader
                        .getResource(location));

        assertEquals(
                "Prompt resource is not readable: "
                        + location,
                exception.getMessage());
    }

    @Test
    void shouldRejectNullConstructorDependencies() {
        NullPointerException resourceLoaderException = assertThrows(
                NullPointerException.class,
                () -> new PromptResourceLoader(
                        null,
                        promptLoader));

        assertEquals(
                "resourceLoader must not be null",
                resourceLoaderException.getMessage());

        NullPointerException promptLoaderException = assertThrows(
                NullPointerException.class,
                () -> new PromptResourceLoader(
                        resourceLoader,
                        null));

        assertEquals(
                "promptLoader must not be null",
                promptLoaderException.getMessage());
    }
}