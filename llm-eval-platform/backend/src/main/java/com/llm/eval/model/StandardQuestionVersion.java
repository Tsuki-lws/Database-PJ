package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "standard_question_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardQuestionVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Integer versionId;
    
    @Column(name = "standard_question_id", nullable = false)
    private Integer standardQuestionId;
    
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "category_id")
    private Integer categoryId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private StandardQuestion.QuestionType questionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private StandardQuestion.DifficultyLevel difficulty;
    
    @Column(name = "version", nullable = false)
    private Integer version;
    
    @Column(name = "changed_by")
    private Integer changedBy;
    
    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_question_id", insertable = false, updatable = false)
    private StandardQuestion standardQuestion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", insertable = false, updatable = false)
    private User changedByUser;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
