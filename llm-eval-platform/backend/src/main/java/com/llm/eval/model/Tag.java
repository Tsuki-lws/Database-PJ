package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "tags")
@Data
public class Tag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tagId;
    
    @Column(name = "tag_name", nullable = false, unique = true)
    private String tagName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "color")
    private String color;
    
    @ManyToMany(mappedBy = "tags")
    private Set<StandardQuestion> questions;
} 