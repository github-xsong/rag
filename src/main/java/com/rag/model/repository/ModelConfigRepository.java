package com.rag.model.repository;

import com.rag.model.entity.ModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ModelConfigRepository extends JpaRepository<ModelConfig, Long> {

    Optional<ModelConfig> findByIsDefaultTrue();

    Optional<ModelConfig> findByName(String name);

    List<ModelConfig> findByEnabledTrue();

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE ModelConfig m SET m.isDefault = false WHERE m.isDefault = true")
    void clearDefault();
}
