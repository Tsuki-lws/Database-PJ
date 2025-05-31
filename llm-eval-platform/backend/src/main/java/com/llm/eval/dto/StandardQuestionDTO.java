package com.llm.eval.dto;

import com.llm.eval.model.StandardQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 导入标准问题的DTO（不包含答案）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardQuestionDTO {
    
    /**
     * 标准化后的问题文本
     */
    private String question;
    
    /**
     * 问题分类ID
     */
    private Integer categoryId;
    
    /**
     * 问题类型
     */
    private String questionType;
    
    /**
     * 问题难度
     */
    private String difficulty;
    
    /**
     * 关联的原始问题ID
     */
    private Integer sourceQuestionId;
    
    /**
     * 问题标签ID列表
     */
    private List<Integer> tags;
    
    private Integer standardQuestionId;
    private String categoryName;
    private StandardQuestion.QuestionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private Set<TagDTO> tagsInfo;
    private boolean hasFinalStandardAnswer;
    
    public static StandardQuestionDTO fromEntity(StandardQuestion entity) {
        if (entity == null) {
            return null;
        }
        
        StandardQuestionDTO dto = new StandardQuestionDTO();
        dto.setStandardQuestionId(entity.getStandardQuestionId());
        dto.setQuestion(entity.getQuestion());
        
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getCategoryId());
            dto.setCategoryName(entity.getCategory().getName());
        }
        
        dto.setQuestionType(entity.getQuestionType().toString());
        dto.setDifficulty(entity.getDifficulty().toString());
        
        if (entity.getSourceQuestion() != null) {
            dto.setSourceQuestionId(entity.getSourceQuestion().getQuestionId());
        }
        
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setVersion(entity.getVersion());
        
        if (entity.getTags() != null) {
            dto.setTagsInfo(entity.getTags().stream()
                    .map(TagDTO::fromEntity)
                    .collect(Collectors.toSet()));
            
            // 设置标签ID列表
            dto.setTags(entity.getTags().stream()
                    .map(tag -> tag.getTagId())
                    .collect(Collectors.toList()));
        }
        
        // Check if the question has a final standard answer
        dto.setHasFinalStandardAnswer(entity.getStandardAnswers() != null && 
                entity.getStandardAnswers().stream().anyMatch(sa -> sa.getIsFinal() != null && sa.getIsFinal()));
        
        return dto;
    }
} 