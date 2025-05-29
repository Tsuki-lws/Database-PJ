package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "evaluation_key_points")
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationKeyPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @ManyToOne
    @JoinColumn(name = "key_point_id", nullable = false)
    private AnswerKeyPoint keyPoint;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private KeyPointStatus status;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum KeyPointStatus {
        MISSED, PARTIAL, MATCHED
    }
} 