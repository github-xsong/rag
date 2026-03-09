package com.rag.chat.service;

import com.rag.chat.dto.ChatRequest;
import com.rag.chat.dto.ChatResponse;
import com.rag.chat.entity.ChatMessage;
import com.rag.chat.entity.ChatSession;
import com.rag.chat.repository.ChatMessageRepository;
import com.rag.chat.repository.ChatSessionRepository;
import com.rag.common.exception.BusinessException;
import com.rag.document.service.VectorStoreService;
import com.rag.knowledge.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStoreService vectorStoreService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    private static final String RAG_PROMPT_TEMPLATE = """
            你是一个基于知识库的智能问答助手。请根据以下参考资料回答用户的问题。
            如果参考资料中没有相关信息，请诚实地告诉用户你不确定，不要编造答案。
            
            参考资料：
            {context}
            
            用户问题：{question}
            """;

    public List<ChatSession> listSessions(Long knowledgeBaseId) {
        return chatSessionRepository.findByKnowledgeBaseIdOrderByUpdatedAtDesc(knowledgeBaseId);
    }

    public List<ChatMessage> getSessionMessages(Long sessionId) {
        if (!chatSessionRepository.existsById(sessionId)) {
            throw new BusinessException("会话不存在: " + sessionId);
        }
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        Long knowledgeBaseId = resolveKnowledgeBaseId(request);
        Long sessionId = resolveSessionId(request, knowledgeBaseId);

        List<Document> relevantDocs = vectorStoreService.search(
                request.getMessage(), knowledgeBaseId, request.getTopK());

        String context = relevantDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        List<Map<String, String>> sources = relevantDocs.stream()
                .map(doc -> {
                    Map<String, String> source = new HashMap<>();
                    source.put("fileName", doc.getMetadata().getOrDefault("fileName", "").toString());
                    source.put("content", doc.getContent().substring(0, Math.min(200, doc.getContent().length())));
                    return source;
                })
                .collect(Collectors.toList());

        saveMessage(sessionId, "USER", request.getMessage(), null, null);

        String prompt = RAG_PROMPT_TEMPLATE
                .replace("{context}", context)
                .replace("{question}", request.getMessage());

        ChatClient chatClient = chatClientBuilder.build();
        String answer = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        String modelName = request.getModelName() != null ? request.getModelName() : "default";
        saveMessage(sessionId, "ASSISTANT", answer, sources, modelName);

        return ChatResponse.builder()
                .sessionId(sessionId)
                .answer(answer)
                .modelName(modelName)
                .sources(sources)
                .build();
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        if (!chatSessionRepository.existsById(sessionId)) {
            throw new BusinessException("会话不存在: " + sessionId);
        }
        chatSessionRepository.deleteById(sessionId);
    }

    private Long resolveKnowledgeBaseId(ChatRequest request) {
        if (request.getKnowledgeBaseId() != null) {
            knowledgeBaseService.getById(request.getKnowledgeBaseId());
            return request.getKnowledgeBaseId();
        }
        return knowledgeBaseService.getDefault().getId();
    }

    private Long resolveSessionId(ChatRequest request, Long knowledgeBaseId) {
        if (request.getSessionId() != null) {
            return request.getSessionId();
        }
        ChatSession session = new ChatSession();
        session.setKnowledgeBaseId(knowledgeBaseId);
        session.setTitle(request.getMessage().substring(0, Math.min(50, request.getMessage().length())));
        return chatSessionRepository.save(session).getId();
    }

    private void saveMessage(Long sessionId, String role, String content,
                             List<Map<String, String>> sources, String modelName) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setSources(sources);
        message.setModelName(modelName);
        chatMessageRepository.save(message);
    }
}
