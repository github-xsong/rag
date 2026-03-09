package com.rag.chat.controller;

import com.rag.chat.dto.ChatRequest;
import com.rag.chat.dto.ChatResponse;
import com.rag.chat.entity.ChatMessage;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.service.ChatService;
import com.rag.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI对话")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送消息")
    @PostMapping
    public R<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return R.ok(chatService.chat(request));
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public R<List<ChatSession>> listSessions(@RequestParam Long knowledgeBaseId) {
        return R.ok(chatService.listSessions(knowledgeBaseId));
    }

    @Operation(summary = "获取会话消息记录")
    @GetMapping("/sessions/{sessionId}/messages")
    public R<List<ChatMessage>> getMessages(@PathVariable Long sessionId) {
        return R.ok(chatService.getSessionMessages(sessionId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public R<Void> deleteSession(@PathVariable Long sessionId) {
        chatService.deleteSession(sessionId);
        return R.ok();
    }
}
