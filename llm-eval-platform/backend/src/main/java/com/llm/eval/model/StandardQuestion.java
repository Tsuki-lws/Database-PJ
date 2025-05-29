package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "standard_questions")
@Data
@SQLDelete(sql = "UPDATE standard_questions SET deleted_at = NOW() WHERE standard_question_id = ?")
@Where(clause = "deleted_at IS NULL")
public class StandardQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standard_question_id")
    private Integer standardQuestionId;
    
    @Column(name = "question", nullable = false)
    private String question;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private QuestionCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private DifficultyLevel difficulty;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_question_id")
    private RawQuestion sourceQuestion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private QuestionStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "version")
    private Integer version;
    
    @ManyToMany
    @JoinTable(
        name = "standard_question_tags",
        joinColumns = @JoinColumn(name = "standard_question_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;
    
    @OneToMany(mappedBy = "standardQuestion")
    private List<StandardAnswer> standardAnswers;
    
    @OneToMany(mappedBy = "standardQuestion")
    private List<CrowdsourcedAnswer> crowdsourcedAnswers;
    
    @OneToMany(mappedBy = "standardQuestion")
    private List<ExpertAnswer> expertAnswers;
    
    public enum QuestionType {
        single_choice, multiple_choice, simple_fact, subjective
    }
    
    public enum DifficultyLevel {
        easy, medium, hard
    }
    
    public enum QuestionStatus {
        draft, pending_review, approved, rejected
    }
} 