package com.rag.chat.repository;

import com.rag.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByKnowledgeBaseIdOrderByUpdatedAtDesc(Long knowledgeBaseId);
}
