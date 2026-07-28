package com.quince.lawyeraiassistant.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rag.retrieval")
public record RetrievalProperties(

                int topK,

                double similarityThreshold

) {
}