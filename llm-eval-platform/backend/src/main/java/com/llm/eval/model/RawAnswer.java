package com.llm.eval.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_answers")
@Data
@SQLDelete(sql = "UPDATE raw_answers SET deleted_at = NOW() WHERE answer_id = ?")
@Where(clause = "deleted_at IS NULL")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RawAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer answerId;
    
    @Column(name = "source_answer_id")
    private Integer sourceAnswerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    @JsonIgnoreProperties({"answers", "hibernateLazyInitializer", "handler"})
    private RawQuestion question;
    
    @Transient // 非持久化字段，用于前端传值
    private Integer questionId;
    
    @Column(name = "answer_body", nullable = false)
    private String answerBody;
    
    @Column(name = "author_info")
    private String authorInfo;
    
    @Column(name = "upvotes")
    private Integer upvotes;
    
    @Column(name = "is_accepted")
    private Boolean isAccepted;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "RawAnswer{" +
               "answerId=" + answerId +
               ", sourceAnswerId=" + sourceAnswerId +
               ", questionId=" + (question != null ? question.getQuestionId() : null) +
               ", answerBody='" + (answerBody != null && answerBody.length() > 50 ? answerBody.substring(0, 50) + "..." : answerBody) + '\'' +
               ", authorInfo='" + authorInfo + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
} 