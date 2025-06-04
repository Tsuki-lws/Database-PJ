package com.llm.eval.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 原始问题实体类
 */
@Entity
@Table(name = "raw_questions")
@Data
@SQLDelete(sql = "UPDATE raw_questions SET deleted_at = NOW() WHERE question_id = ?")
@Where(clause = "deleted_at IS NULL")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RawQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;
    
    @Column(name = "source_question_id")
    private Integer sourceQuestionId;
    
    @Column(name = "question_title")
    private String questionTitle;
    
    @Column(name = "question_body", nullable = false)
    private String questionBody;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "source_id")
    private Integer sourceId;
    
    @Column(name = "source_url")
    private String sourceUrl;
    
    @Column(name = "crawled_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crawledAt;
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonIgnoreProperties({"question", "questionId"})
    private List<RawAnswer> answers;
    
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (crawledAt == null) {
            crawledAt = now;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "RawQuestion{" +
               "questionId=" + questionId +
               ", sourceQuestionId=" + sourceQuestionId +
               ", questionTitle='" + questionTitle + '\'' +
               ", source='" + source + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
} 