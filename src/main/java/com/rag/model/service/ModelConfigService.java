package com.rag.model.service;

import com.rag.common.exception.BusinessException;
import com.rag.model.entity.ModelConfig;
import com.rag.model.repository.ModelConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigService {

    private final ModelConfigRepository modelConfigRepository;

    public List<ModelConfig> list() {
        return modelConfigRepository.findAll();
    }

    public List<ModelConfig> listEnabled() {
        return modelConfigRepository.findByEnabledTrue();
    }

    public ModelConfig getById(Long id) {
        return modelConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模型配置不存在: " + id));
    }

    public ModelConfig getByName(String name) {
        return modelConfigRepository.findByName(name)
                .orElseThrow(() -> new BusinessException("模型配置不存在: " + name));
    }

    public ModelConfig getDefault() {
        return modelConfigRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new BusinessException("未设置默认模型"));
    }

    @Transactional
    public ModelConfig create(ModelConfig config) {
        if (modelConfigRepository.existsByName(config.getName())) {
            throw new BusinessException("模型配置名称已存在: " + config.getName());
        }
        if (Boolean.TRUE.equals(config.getIsDefault())) {
            modelConfigRepository.clearDefault();
        }
        return modelConfigRepository.save(config);
    }

    @Transactional
    public ModelConfig update(Long id, ModelConfig updated) {
        ModelConfig existing = getById(id);
        existing.setName(updated.getName());
        existing.setProvider(updated.getProvider());
        existing.setModelName(updated.getModelName());
        existing.setApiKey(updated.getApiKey());
        existing.setBaseUrl(updated.getBaseUrl());
        existing.setEnabled(updated.getEnabled());
        return modelConfigRepository.save(existing);
    }

    @Transactional
    public void setDefault(Long id) {
        getById(id);
        modelConfigRepository.clearDefault();
        ModelConfig config = getById(id);
        config.setIsDefault(true);
        modelConfigRepository.save(config);
    }

    @Transactional
    public void delete(Long id) {
        if (!modelConfigRepository.existsById(id)) {
            throw new BusinessException("模型配置不存在: " + id);
        }
        modelConfigRepository.deleteById(id);
    }
}
