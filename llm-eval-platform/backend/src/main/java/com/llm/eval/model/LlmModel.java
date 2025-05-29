package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "llm_models", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "version"})
})
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE llm_models SET deleted_at = now() WHERE model_id = ?")
@Where(clause = "deleted_at IS NULL")
public class LlmModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Integer modelId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "provider")
    private String provider;

    @Column(name = "description")
    private String description;

    @Column(name = "api_config", columnDefinition = "JSON")
    private String apiConfig;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 