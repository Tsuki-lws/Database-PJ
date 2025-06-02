package com.llmeval.dto;

import lombok.Data;
import java.util.List;

/**
 * 评测详情DTO
 */
@Data
public class EvaluationDetailDTO {
    private Integer answerId;
    private String question;
    private String questionType;
    private String modelName;
    private String modelVersion;
    private String modelAnswer;
    private String standardAnswer;
    private List<KeyPointDTO> keyPoints;
    private List<OptionDTO> options;
    private String categoryName;
    
    @Data
    public static class KeyPointDTO {
        private Integer keyPointId;
        private String pointText;
        private Double pointWeight;
        private String pointType;
        private String exampleText;
    }
    
    @Data
    public static class OptionDTO {
        private Integer optionId;
        private String optionCode;
        private String optionText;
        private Boolean isCorrect;
    }
} 