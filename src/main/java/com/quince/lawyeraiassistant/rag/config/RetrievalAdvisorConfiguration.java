package com.quince.lawyeraiassistant.rag.config;

import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetrievalAdvisorConfiguration {

    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
            DocumentRetriever documentRetriever) {

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }

}