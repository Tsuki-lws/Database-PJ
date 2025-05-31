package com.llm.eval.dto;

import java.time.LocalDateTime;

/**
 * 标准问题版本DTO
 */
public class StandardQuestionVersionDTO {
      private Integer versionId;
    private Integer standardQuestionId;
    private String question;
    private Integer categoryId;
    private String questionType;
    private String difficulty;
    private Integer version;
    private Integer changedBy;
    private String changeReason;
    private LocalDateTime createdAt;
    private Boolean isLatest;
    private String status;
    
    // Constructors
    public StandardQuestionVersionDTO() {}
      public StandardQuestionVersionDTO(Integer versionId, Integer standardQuestionId, String question, 
                                    Integer version, String changeReason, LocalDateTime createdAt) {
        this.versionId = versionId;
        this.standardQuestionId = standardQuestionId;
        this.question = question;
        this.version = version;
        this.changeReason = changeReason;
        this.createdAt = createdAt;
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
    
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    public Integer getChangedBy() { return changedBy; }
    public void setChangedBy(Integer changedBy) { this.changedBy = changedBy; }
    
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Boolean getIsLatest() { return isLatest; }
    public void setIsLatest(Boolean isLatest) { this.isLatest = isLatest; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
