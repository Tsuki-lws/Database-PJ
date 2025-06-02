package com.llmeval.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 人工评测提交DTO
 */
@Data
public class ManualEvaluationDTO {
    private Integer answerId;
    private Double score;
    private Boolean isCorrect;
    private List<String> keyPointsStatus;
    private Map<String, Integer> dimensions;
    private String comments;
} 