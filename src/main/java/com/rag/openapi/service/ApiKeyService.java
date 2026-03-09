package com.rag.openapi.service;

import com.rag.common.exception.BusinessException;
import com.rag.openapi.entity.ApiKeyEntity;
import com.rag.openapi.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public List<ApiKeyEntity> list() {
        return apiKeyRepository.findAll();
    }

    @Transactional
    public ApiKeyEntity create(ApiKeyEntity entity) {
        if (apiKeyRepository.existsByName(entity.getName())) {
            throw new BusinessException("API Key名称已存在: " + entity.getName());
        }
        entity.setApiKey(generateApiKey());
        return apiKeyRepository.save(entity);
    }

    @Transactional
    public void toggleEnabled(Long id) {
        ApiKeyEntity entity = apiKeyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("API Key不存在: " + id));
        entity.setEnabled(!entity.getEnabled());
        apiKeyRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new BusinessException("API Key不存在: " + id);
        }
        apiKeyRepository.deleteById(id);
    }

    public ApiKeyEntity validateAndGet(String apiKey) {
        ApiKeyEntity entity = apiKeyRepository.findByApiKeyAndEnabledTrue(apiKey)
                .orElseThrow(() -> new BusinessException(401, "无效或已禁用的API Key"));
        entity.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(entity);
        return entity;
    }

    private String generateApiKey() {
        return "rag-" + UUID.randomUUID().toString().replace("-", "");
    }
}
