package com.rag.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ChatResponse {

    private Long sessionId;

    private String answer;

    private String modelName;

    private List<Map<String, String>> sources;
}
