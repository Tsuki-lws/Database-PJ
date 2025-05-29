package com.llm.eval.dto;

import com.llm.eval.model.QuestionCategory;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryDTO {
    
    private Integer categoryId;
    private String name;
    private String description;
    private Integer parentId;
    private String parentName;
    private List<CategoryDTO> children;
    private long questionCount;
    
    public static CategoryDTO fromEntity(QuestionCategory entity) {
        return fromEntity(entity, false);
    }
    
    public static CategoryDTO fromEntity(QuestionCategory entity, boolean includeChildren) {
        if (entity == null) {
            return null;
        }
        
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(entity.getCategoryId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        
        if (entity.getParent() != null) {
            dto.setParentId(entity.getParent().getCategoryId());
            dto.setParentName(entity.getParent().getName());
        }
        
        // Only include children if requested
        if (includeChildren && entity.getChildren() != null) {
            dto.setChildren(entity.getChildren().stream()
                    .map(child -> fromEntity(child, false)) // Avoid infinite recursion
                    .collect(Collectors.toList()));
        }
        
        // Set question count if available
        if (entity.getQuestions() != null) {
            dto.setQuestionCount(entity.getQuestions().size());
        }
        
        return dto;
    }
} 