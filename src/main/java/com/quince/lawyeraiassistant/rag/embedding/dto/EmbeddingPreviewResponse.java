package com.quince.lawyeraiassistant.rag.embedding.dto;

import java.util.List;

public record EmbeddingPreviewResponse(

        String text,

        int dimension,

        List<Float> preview

) {
}