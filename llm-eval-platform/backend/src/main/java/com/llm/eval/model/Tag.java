package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@ToString(exclude = {"questions"})
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
    @JsonIgnore
    private Set<StandardQuestion> questions;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Tag tag = (Tag) o;
        return tagId != null && tagId.equals(tag.tagId);
    }
    
    @Override
    public int hashCode() {
        // 只使用ID计算hashCode，避免使用questions字段导致的循环引用
        return tagId != null ? tagId.hashCode() : 0;
    }
} 