package com.llm.eval.entity;

import jakarta.persistence.*;
import com.llm.eval.model.StandardQuestion;
import java.time.LocalDateTime;

/**
 * 标准问题版本实体
 */
@Entity
@Table(name = "standard_question_versions")
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
    
    // 关联的标准问题
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_question_id", insertable = false, updatable = false)
    private StandardQuestion standardQuestion;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 枚举定义
    public enum QuestionType {
        single_choice, multiple_choice, simple_fact, subjective
    }
    
    public enum Difficulty {
        easy, medium, hard
    }
    
    // Constructors
    public StandardQuestionVersion() {}
      public StandardQuestionVersion(Integer standardQuestionId, String question, Integer version, String changeReason) {
        this.standardQuestionId = standardQuestionId;
        this.question = question;
        this.version = version;
        this.changeReason = changeReason;
    }
      // Getters and Setters
    public Integer getVersionId() { return versionId; }
    public void setVersionId(Integer versionId) { this.versionId = versionId; }
    
    public Integer getStandardQuestionId() { return standardQuestionId; }
    public void setStandardQuestionId(Integer standardQuestionId) { this.standardQuestionId = standardQuestionId; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
      public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
      public StandardQuestion.QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(StandardQuestion.QuestionType questionType) { this.questionType = questionType; }
    
    public StandardQuestion.DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(StandardQuestion.DifficultyLevel difficulty) { this.difficulty = difficulty; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
      public Integer getChangedBy() { return changedBy; }
    public void setChangedBy(Integer changedBy) { this.changedBy = changedBy; }
    
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public StandardQuestion getStandardQuestion() { return standardQuestion; }
    public void setStandardQuestion(StandardQuestion standardQuestion) { this.standardQuestion = standardQuestion; }
    
    @Override
    public String toString() {
        return "StandardQuestionVersion{" +
                "versionId=" + versionId +
                ", standardQuestionId=" + standardQuestionId +
                ", question='" + question + '\'' +
                ", version=" + version +
                ", changeReason='" + changeReason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
