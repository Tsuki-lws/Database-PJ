package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;

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
    private QuestionCategory parent;
    
    @OneToMany(mappedBy = "parent")
    private List<QuestionCategory> children;
    
    @OneToMany(mappedBy = "category")
    private List<StandardQuestion> questions;
} 