package com.quince.lawyeraiassistant.prompt.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.quince.lawyeraiassistant.prompt.model.PromptFragment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClasspathPromptLoader
        implements PromptLoader {

    private final ResourceLoader resourceLoader;

    @Override
    public PromptFragment load(String location) {

        String content = readPrompt(location);

        return buildFragment(location, content);

    }

    private String readPrompt(String location) {
        Resource resource = resourceLoader.getResource(location);

        if (!resource.exists()) {
            throw new IllegalStateException(
                    "Prompt resource does not exist: " + location);
        }

        if (!resource.isReadable()) {
            throw new IllegalStateException(
                    "Prompt resource is not readable: " + location);
        }

        try {
            String content = resource.getContentAsString(
                    StandardCharsets.UTF_8);

            if (content.isBlank()) {
                throw new IllegalStateException(
                        "Prompt resource is empty: " + location);
            }

            return content.strip();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to read prompt resource: " + location,
                    exception);
        }
    }

    private PromptFragment buildFragment(
            String location,
            String content) {

        return PromptFragment.builder()
                .name(extractName(location))
                .content(content)
                .version("v1")
                .source(location)
                .build();

    }

    private String extractName(String location) {

        String filename = location.substring(location.lastIndexOf('/') + 1);

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }

        return filename;

    }

}