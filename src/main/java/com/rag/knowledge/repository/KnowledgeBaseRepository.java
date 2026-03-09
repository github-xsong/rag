package com.rag.knowledge.repository;

import com.rag.knowledge.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    Optional<KnowledgeBase> findByIsDefaultTrue();

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE KnowledgeBase kb SET kb.isDefault = false WHERE kb.isDefault = true")
    void clearDefault();
}
