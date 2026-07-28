package com.quince.lawyeraiassistant.rag.vector.initializer;

import com.quince.lawyeraiassistant.rag.vector.config.KnowledgeBaseProperties;
import com.quince.lawyeraiassistant.rag.vector.service.DocumentLoadingService;
import com.quince.lawyeraiassistant.rag.vector.service.SimpleVectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseInitializer implements ApplicationRunner {

    private final KnowledgeBaseProperties properties;
    private final DocumentLoadingService documentLoadingService;
    private final SimpleVectorStoreService vectorStoreService;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            log.info("Knowledge-base initialization is disabled.");
            return;
        }

        log.info(
                "Starting knowledge-base initialization. location={}",
                properties.location());

        long startTime = System.currentTimeMillis();

        try {
            List<Document> chunks = documentLoadingService.loadAndSplit(properties.location());

            if (chunks.isEmpty()) {
                log.warn(
                        "No knowledge-base documents were found. location={}",
                        properties.location());
                return;
            }

            int storedCount = vectorStoreService.addDocuments(chunks);
            long duration = System.currentTimeMillis() - startTime;

            log.info(
                    "Knowledge-base initialization completed. chunks={}, durationMs={}",
                    storedCount,
                    duration);
        } catch (Exception exception) {
            log.error(
                    "Knowledge-base initialization failed. location={}",
                    properties.location(),
                    exception);

            throw new IllegalStateException(
                    "Failed to initialize knowledge base",
                    exception);
        }
    }
}