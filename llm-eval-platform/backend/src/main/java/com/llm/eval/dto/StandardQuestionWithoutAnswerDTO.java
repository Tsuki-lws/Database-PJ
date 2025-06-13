package com.llm.eval.dto;

import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.StandardQuestion;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<StandardAnswerDTO> standardAnswers;
    
    @Data
    public static class StandardAnswerDTO {
        private Integer standardAnswerId;
        private String answer;
        private Boolean isFinal;
    }
    
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
        
        if (question.getStandardAnswers() != null && !question.getStandardAnswers().isEmpty()) {
            dto.setStandardAnswers(question.getStandardAnswers().stream()
                    .map(answer -> {
                        StandardAnswerDTO answerDTO = new StandardAnswerDTO();
                        answerDTO.setStandardAnswerId(answer.getStandardAnswerId());
                        answerDTO.setAnswer(answer.getAnswer());
                        answerDTO.setIsFinal(answer.getIsFinal());
                        return answerDTO;
                    })
                    .collect(Collectors.toList()));
        } else {
            dto.setStandardAnswers(Collections.emptyList());
        }
        
        return dto;
    }
}
