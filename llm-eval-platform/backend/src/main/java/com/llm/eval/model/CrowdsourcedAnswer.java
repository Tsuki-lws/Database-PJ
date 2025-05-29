package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crowdsourced_answers")
@Data
@SQLDelete(sql = "UPDATE crowdsourced_answers SET deleted_at = NOW() WHERE answer_id = ?")
@Where(clause = "deleted_at IS NULL")
public class CrowdsourcedAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Integer answerId;
    
    @Column(name = "task_id")
    private Integer taskId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_question_id", nullable = false)
    private StandardQuestion standardQuestion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "answer_text", nullable = false)
    private String answerText;
    
    @Column(name = "submission_time")
    private LocalDateTime submissionTime;
    
    @Column(name = "quality_score")
    private BigDecimal qualityScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ReviewStatus reviewStatus;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
    
    @Column(name = "review_time")
    private LocalDateTime reviewTime;
    
    @Column(name = "review_comment")
    private String reviewComment;
    
    @Column(name = "is_selected")
    private Boolean isSelected;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    public enum ReviewStatus {
        pending, approved, rejected
    }
} 