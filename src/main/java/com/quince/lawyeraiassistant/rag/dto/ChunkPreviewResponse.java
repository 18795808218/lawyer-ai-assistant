package com.quince.lawyeraiassistant.rag.dto;

import java.util.Map;

public record ChunkPreviewResponse(

        int index,

        int length,

        String content,

        Map<String, Object> metadata

) {
}