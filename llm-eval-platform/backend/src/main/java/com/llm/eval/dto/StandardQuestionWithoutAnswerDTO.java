package com.llm.eval.dto;

import com.llm.eval.model.StandardQuestion;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StandardQuestionWithoutAnswerDTO {
    private Integer standardQuestionId;
    private String question;
    private Integer categoryId;
    private String categoryName;
    private StandardQuestion.QuestionType questionType;
    private StandardQuestion.DifficultyLevel difficulty;
    private Integer sourceQuestionId;
    private List<Integer> tagIds;
    private List<String> tagNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    
    public static StandardQuestionWithoutAnswerDTO fromEntity(StandardQuestion question) {
        StandardQuestionWithoutAnswerDTO dto = new StandardQuestionWithoutAnswerDTO();
        dto.setStandardQuestionId(question.getStandardQuestionId());
        dto.setQuestion(question.getQuestion());
        dto.setQuestionType(question.getQuestionType());
        dto.setDifficulty(question.getDifficulty());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        dto.setVersion(question.getVersion());
        
        if (question.getCategory() != null) {
            dto.setCategoryId(question.getCategory().getCategoryId());
            dto.setCategoryName(question.getCategory().getName());
        }
        
        if (question.getSourceQuestion() != null) {
            dto.setSourceQuestionId(question.getSourceQuestion().getQuestionId());
        }
        
        if (question.getTags() != null && !question.getTags().isEmpty()) {
            dto.setTagIds(question.getTags().stream()
                    .map(tag -> tag.getTagId())
                    .toList());
            dto.setTagNames(question.getTags().stream()
                    .map(tag -> tag.getTagName())
                    .toList());
        }
        
        return dto;
    }
}
