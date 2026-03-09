package com.rag.openapi.repository;

import com.rag.openapi.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

    Optional<ApiKeyEntity> findByApiKeyAndEnabledTrue(String apiKey);

    boolean existsByName(String name);
}
