package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "question_categories")
@Data
public class QuestionCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private QuestionCategory parent;
    
    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    @ToString.Exclude
    private List<QuestionCategory> children;
    
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    @ToString.Exclude
    private List<StandardQuestion> questions;
    
    @Transient // 不持久化到数据库
    private Long questionCount;
} 