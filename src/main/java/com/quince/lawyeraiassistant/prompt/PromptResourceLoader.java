package com.quince.lawyeraiassistant.prompt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class PromptResourceLoader {

    private final ResourceLoader resourceLoader;

    /*
     * 缓存已经读取过的 Prompt。
     *
     * key：classpath:prompts/system/lawyer-system.txt
     * value：Prompt 文件的文本内容
     */
    private final Map<String, String> promptCache = new ConcurrentHashMap<>();

    public PromptResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 加载指定位置的 Prompt。
     */
    public String load(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt resource location must not be blank");
        }

        return promptCache.computeIfAbsent(
                location,
                this::readPrompt);
    }

    /**
     * 实际读取 Prompt 文件。
     */
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

    public Resource getResource(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Prompt resource location must not be blank");
        }

        Resource resource = resourceLoader.getResource(location);

        if (!resource.exists()) {
            throw new IllegalStateException(
                    "Prompt resource does not exist: " + location);
        }

        if (!resource.isReadable()) {
            throw new IllegalStateException(
                    "Prompt resource is not readable: " + location);
        }

        return resource;
    }
}