package com.rag.openapi.controller;

import com.rag.chat.dto.ChatRequest;
import com.rag.chat.dto.ChatResponse;
import com.rag.chat.service.ChatService;
import com.rag.common.response.R;
import com.rag.openapi.entity.ApiKeyEntity;
import com.rag.openapi.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "开放API")
@RestController
@RequiredArgsConstructor
public class OpenApiController {

    private final ApiKeyService apiKeyService;
    private final ChatService chatService;

    // ===== 对外开放接口 =====

    @Operation(summary = "开放接口 - AI问答")
    @PostMapping("/api/open/chat")
    public R<ChatResponse> openChat(
            @RequestHeader("X-API-Key") String apiKey,
            @Valid @RequestBody ChatRequest request) {
        ApiKeyEntity keyEntity = apiKeyService.validateAndGet(apiKey);
        if (keyEntity.getKnowledgeBaseId() != null) {
            request.setKnowledgeBaseId(keyEntity.getKnowledgeBaseId());
        }
        return R.ok(chatService.chat(request));
    }

    // ===== API Key 管理接口 =====

    @Operation(summary = "获取API Key列表")
    @GetMapping("/api/api-keys")
    public R<List<ApiKeyEntity>> listKeys() {
        return R.ok(apiKeyService.list());
    }

    @Operation(summary = "创建API Key")
    @PostMapping("/api/api-keys")
    public R<ApiKeyEntity> createKey(@Valid @RequestBody ApiKeyEntity entity) {
        return R.ok(apiKeyService.create(entity));
    }

    @Operation(summary = "启用/禁用API Key")
    @PutMapping("/api/api-keys/{id}/toggle")
    public R<Void> toggleKey(@PathVariable Long id) {
        apiKeyService.toggleEnabled(id);
        return R.ok();
    }

    @Operation(summary = "删除API Key")
    @DeleteMapping("/api/api-keys/{id}")
    public R<Void> deleteKey(@PathVariable Long id) {
        apiKeyService.delete(id);
        return R.ok();
    }
}
