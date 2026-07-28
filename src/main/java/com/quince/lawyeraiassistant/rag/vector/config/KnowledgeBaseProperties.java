package com.quince.lawyeraiassistant.rag.vector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rag.knowledge-base")
public record KnowledgeBaseProperties(
        boolean enabled,
        String location) {
}