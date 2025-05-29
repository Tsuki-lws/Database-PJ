package com.llm.eval.dto;

import com.llm.eval.model.StandardQuestion;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class StandardQuestionDTO {
    
    private Integer standardQuestionId;
    private String question;
    private Integer categoryId;
    private String categoryName;
    private StandardQuestion.QuestionType questionType;
    private StandardQuestion.DifficultyLevel difficulty;
    private Integer sourceQuestionId;
    private StandardQuestion.QuestionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private Set<TagDTO> tags;
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
        
        dto.setQuestionType(entity.getQuestionType());
        dto.setDifficulty(entity.getDifficulty());
        
        if (entity.getSourceQuestion() != null) {
            dto.setSourceQuestionId(entity.getSourceQuestion().getQuestionId());
        }
        
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setVersion(entity.getVersion());
        
        if (entity.getTags() != null) {
            dto.setTags(entity.getTags().stream()
                    .map(TagDTO::fromEntity)
                    .collect(Collectors.toSet()));
        }
        
        // Check if the question has a final standard answer
        dto.setHasFinalStandardAnswer(entity.getStandardAnswers() != null && 
                entity.getStandardAnswers().stream().anyMatch(sa -> sa.getIsFinal() != null && sa.getIsFinal()));
        
        return dto;
    }
} 