package com.rag.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "model_config")
public class ModelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "model_name", nullable = false, length = 50)
    private String modelName;

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "base_url", length = 300)
    private String baseUrl;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
