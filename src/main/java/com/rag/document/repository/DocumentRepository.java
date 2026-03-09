package com.rag.document.repository;

import com.rag.common.enums.DocumentStatus;
import com.rag.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByKnowledgeBaseIdOrderByCreatedAtDesc(Long knowledgeBaseId);

    List<Document> findByStatus(DocumentStatus status);

    long countByKnowledgeBaseId(Long knowledgeBaseId);
}
