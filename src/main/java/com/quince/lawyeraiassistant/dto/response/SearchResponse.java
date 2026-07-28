package com.quince.lawyeraiassistant.dto.response;

import java.util.Map;

public record SearchResponse(

        String content,

        Map<String, Object> metadata

) {
}