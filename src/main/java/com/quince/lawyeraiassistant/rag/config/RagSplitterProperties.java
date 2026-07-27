package com.quince.lawyeraiassistant.rag.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rag.splitter")
public record RagSplitterProperties(

        @Min(value = 50, message = "chunk-size 不能小于 50") @Max(value = 8192, message = "chunk-size 不能大于 8192") 
        int chunkSize,

        @Min(value = 1, message = "min-chunk-size-chars 不能小于 1") 
        int minChunkSizeChars,

        @Min(value = 1, message = "min-chunk-length-to-embed 不能小于 1") 
        int minChunkLengthToEmbed,

        @Min(value = 1, message = "max-num-chunks 不能小于 1") @Max(value = 100000, message = "max-num-chunks 不能大于 100000") 
        int maxNumChunks,

        boolean keepSeparator

) {
}