package com.quince.lawyeraiassistant.prompt.registry;

import com.quince.lawyeraiassistant.prompt.definition.PromptDefinition;
import com.quince.lawyeraiassistant.prompt.loader.PromptLoader;
import com.quince.lawyeraiassistant.prompt.model.PromptFragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromptRegistryInitializerTest {

    private PromptLoader promptLoader;

    private PromptRegistry promptRegistry;

    private PromptRegistryInitializer initializer;

    @BeforeEach
    void setUp() {
        promptLoader = mock(PromptLoader.class);
        promptRegistry = mock(PromptRegistry.class);

        for (PromptDefinition definition : PromptDefinition.values()) {
            PromptFragment loadedFragment = PromptFragment.builder()
                    .name(definition.getName())
                    .content("content for " + definition.getName())
                    .version(definition.getVersion())
                    .source(definition.getLocation())
                    .build();

            when(promptLoader.load(definition.getLocation()))
                    .thenReturn(loadedFragment);
        }

        initializer = new PromptRegistryInitializer(
                promptLoader,
                promptRegistry);
    }

    @Test
    void shouldLoadAndRegisterLawyerSystemPrompt() {
        PromptFragment loadedFragment = PromptFragment.builder()
                .name("lawyer-system")
                .content("你是一名专业法律助手。")
                .version("v1")
                .source(PromptDefinition.LAWYER_SYSTEM.getLocation())
                .build();

        when(
                promptLoader.load(
                        PromptDefinition.LAWYER_SYSTEM.getLocation()))
                .thenReturn(loadedFragment);

        initializer.initialize();

        verify(promptLoader).load(
                PromptDefinition.LAWYER_SYSTEM.getLocation());

        verify(promptRegistry).register(
                org.mockito.ArgumentMatchers.argThat(
                        fragment -> PromptDefinition.LAWYER_SYSTEM.getName().equals(
                                fragment.getName())
                                && "你是一名专业法律助手。"
                                        .equals(
                                                fragment.getContent())
                                && "v1".equals(
                                        fragment.getVersion())
                                && PromptDefinition.LAWYER_SYSTEM.getLocation()
                                        .equals(
                                                fragment.getSource())));
    }

    @Test
    void shouldNormalizeLoadedPromptName() {
        PromptFragment loadedFragment = PromptFragment.builder()
                .name("physical-file-name")
                .content("系统提示词")
                .version("v1")
                .source(PromptDefinition.LAWYER_SYSTEM.getLocation())
                .build();

        when(
                promptLoader.load(
                        PromptDefinition.LAWYER_SYSTEM.getLocation()))
                .thenReturn(loadedFragment);

        DefaultPromptRegistry realRegistry = new DefaultPromptRegistry();

        PromptRegistryInitializer realInitializer = new PromptRegistryInitializer(
                promptLoader,
                realRegistry);

        realInitializer.initialize();

        PromptFragment registered = realRegistry.find(
                PromptDefinition.LAWYER_SYSTEM.getName());

        assertEquals(
                PromptDefinition.LAWYER_SYSTEM.getName(),
                registered.getName());

        assertEquals(
                "系统提示词",
                registered.getContent());

        assertEquals(
                "v1",
                registered.getVersion());
    }

    @Test
    void shouldPropagateLoaderException() {
        IllegalStateException expected = new IllegalStateException(
                "Prompt resource does not exist");

        when(
                promptLoader.load(
                        PromptDefinition.LAWYER_SYSTEM.getLocation()))
                .thenThrow(expected);

        IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                initializer::initialize);

        assertSame(expected, actual);
    }

    @Test
    void shouldPropagateRegistryException() {
        PromptFragment loadedFragment = PromptFragment.builder()
                .name("lawyer-system")
                .content("系统提示词")
                .version("v1")
                .source(PromptDefinition.LAWYER_SYSTEM.getLocation())
                .build();

        when(
                promptLoader.load(
                        PromptDefinition.LAWYER_SYSTEM.getLocation()))
                .thenReturn(loadedFragment);

        IllegalStateException expected = new IllegalStateException(
                "Prompt already registered");

        org.mockito.Mockito.doThrow(expected)
                .when(promptRegistry)
                .register(
                        org.mockito.ArgumentMatchers.any(
                                PromptFragment.class));

        IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                initializer::initialize);

        assertSame(expected, actual);
    }

    @Test
    void shouldRejectNullDependencies() {
        NullPointerException loaderException = assertThrows(
                NullPointerException.class,
                () -> new PromptRegistryInitializer(
                        null,
                        promptRegistry));

        assertEquals(
                "promptLoader must not be null",
                loaderException.getMessage());

        NullPointerException registryException = assertThrows(
                NullPointerException.class,
                () -> new PromptRegistryInitializer(
                        promptLoader,
                        null));

        assertEquals(
                "promptRegistry must not be null",
                registryException.getMessage());
    }
}
