package com.rag.knowledge.service;

import com.rag.common.exception.BusinessException;
import com.rag.knowledge.entity.KnowledgeBase;
import com.rag.knowledge.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public List<KnowledgeBase> list() {
        return knowledgeBaseRepository.findAll();
    }

    public KnowledgeBase getById(Long id) {
        return knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识库不存在: " + id));
    }

    public KnowledgeBase getDefault() {
        return knowledgeBaseRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new BusinessException("未设置默认知识库"));
    }

    @Transactional
    public KnowledgeBase create(KnowledgeBase knowledgeBase) {
        if (knowledgeBaseRepository.existsByName(knowledgeBase.getName())) {
            throw new BusinessException("知识库名称已存在: " + knowledgeBase.getName());
        }
        if (Boolean.TRUE.equals(knowledgeBase.getIsDefault())) {
            knowledgeBaseRepository.clearDefault();
        }
        return knowledgeBaseRepository.save(knowledgeBase);
    }

    @Transactional
    public KnowledgeBase update(Long id, KnowledgeBase updated) {
        KnowledgeBase existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return knowledgeBaseRepository.save(existing);
    }

    @Transactional
    public void setDefault(Long id) {
        getById(id);
        knowledgeBaseRepository.clearDefault();
        KnowledgeBase kb = getById(id);
        kb.setIsDefault(true);
        knowledgeBaseRepository.save(kb);
    }

    @Transactional
    public void delete(Long id) {
        if (!knowledgeBaseRepository.existsById(id)) {
            throw new BusinessException("知识库不存在: " + id);
        }
        knowledgeBaseRepository.deleteById(id);
    }
}
