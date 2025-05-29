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
@Table(name = "llm_answers")
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE llm_answers SET deleted_at = now() WHERE llm_answer_id = ?")
@Where(clause = "deleted_at IS NULL")
public class LlmAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "llm_answer_id")
    private Integer llmAnswerId;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private LlmModel model;

    @ManyToOne
    @JoinColumn(name = "standard_question_id", nullable = false)
    private StandardQuestion standardQuestion;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = false)
    private DatasetVersion datasetVersion;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "latency")
    private Integer latency;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "prompt_template", columnDefinition = "TEXT")
    private String promptTemplate;

    @Column(name = "prompt_params", columnDefinition = "JSON")
    private String promptParams;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "top_p")
    private Double topP;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private EvaluationBatch batch;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "is_final")
    private Boolean isFinal = true;

    @ManyToOne
    @JoinColumn(name = "parent_answer_id")
    private LlmAnswer parentAnswer;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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