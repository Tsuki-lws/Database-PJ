package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "evaluations")
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id")
    private Integer evaluationId;

    @ManyToOne
    @JoinColumn(name = "llm_answer_id", nullable = false)
    private LlmAnswer llmAnswer;

    @ManyToOne
    @JoinColumn(name = "standard_answer_id", nullable = false)
    private StandardAnswer standardAnswer;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "method")
    private String method;

    @Column(name = "key_points_evaluation", columnDefinition = "JSON")
    private String keyPointsEvaluation;

    @ManyToOne
    @JoinColumn(name = "judge_model_id")
    private LlmModel judgeModel;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private EvaluationBatch batch;

    @Column(name = "comments")
    private String comments;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 