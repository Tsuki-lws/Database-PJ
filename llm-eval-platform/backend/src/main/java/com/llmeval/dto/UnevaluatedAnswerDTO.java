package com.llmeval.dto;

import lombok.Data;

/**
 * 待评测答案DTO
 */
@Data
public class UnevaluatedAnswerDTO {
    private Integer answerId;
    private Integer questionId;
    private String question;
    private String modelName;
    private String modelVersion;
    private String questionType;
    private String status;
    private String categoryName;
} 