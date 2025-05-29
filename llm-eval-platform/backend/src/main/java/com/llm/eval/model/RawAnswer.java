package com.llm.eval.model;

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
public class RawAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer answerId;
    
    @Column(name = "source_answer_id")
    private Integer sourceAnswerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private RawQuestion question;
    
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
} 