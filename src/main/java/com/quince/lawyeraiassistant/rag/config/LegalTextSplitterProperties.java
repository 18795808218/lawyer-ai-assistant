package com.quince.lawyeraiassistant.rag.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rag.legal-splitter")
public record LegalTextSplitterProperties(

        @Min(value = 100, message = "chunk-size 不能小于 100") int chunkSize,

        @Min(value = 0, message = "chunk-overlap 不能小于 0") int chunkOverlap,

        @Min(value = 0, message = "boundary-search-size 不能小于 0") int boundarySearchSize,

        @Min(value = 1, message = "min-chunk-size 不能小于 1") int minChunkSize

) {

    public LegalTextSplitterProperties {

        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "chunk-overlap 必须小于 chunk-size");
        }

        if (boundarySearchSize >= chunkSize) {
            throw new IllegalArgumentException(
                    "boundary-search-size 必须小于 chunk-size");
        }

        if (minChunkSize > chunkSize) {
            throw new IllegalArgumentException(
                    "min-chunk-size 不能大于 chunk-size");
        }
    }
}