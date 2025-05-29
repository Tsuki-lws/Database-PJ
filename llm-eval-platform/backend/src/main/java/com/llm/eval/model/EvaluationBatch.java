package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "evaluation_batches")
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Integer batchId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private LlmModel model;

    @ManyToOne
    @JoinColumn(name = "judge_model_id")
    private LlmModel judgeModel;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private DatasetVersion datasetVersion;

    @Column(name = "evaluation_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private EvaluationMethod evaluationMethod;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EvaluationStatus status;

    @Column(name = "metrics_summary", columnDefinition = "JSON")
    private String metricsSummary;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = EvaluationStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EvaluationMethod {
        HUMAN, AUTO, JUDGE_MODEL
    }

    public enum EvaluationStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
} 