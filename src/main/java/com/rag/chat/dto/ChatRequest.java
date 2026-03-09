package com.rag.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    private Long sessionId;

    private Long knowledgeBaseId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String modelName;

    private Integer topK = 5;
}
