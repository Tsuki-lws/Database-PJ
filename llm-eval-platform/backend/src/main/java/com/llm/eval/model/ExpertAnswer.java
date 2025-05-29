package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "expert_answers")
@Data
@SQLDelete(sql = "UPDATE expert_answers SET deleted_at = NOW() WHERE expert_answer_id = ?")
@Where(clause = "deleted_at IS NULL")
public class ExpertAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expert_answer_id")
    private Integer expertAnswerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_question_id", nullable = false)
    private StandardQuestion standardQuestion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private User expert;
    
    @Column(name = "answer_text", nullable = false)
    private String answerText;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "expertise_level")
    private ExpertiseLevel expertiseLevel;
    
    @Column(name = "submission_time")
    private LocalDateTime submissionTime;
    
    @Column(name = "is_verified")
    private Boolean isVerified;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verifier_id")
    private User verifier;
    
    @Column(name = "verification_time")
    private LocalDateTime verificationTime;
    
    @Column(name = "verification_comment")
    private String verificationComment;
    
    @Column(name = "is_selected_as_standard")
    private Boolean isSelectedAsStandard;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    public enum ExpertiseLevel {
        junior, senior, authority
    }
} 