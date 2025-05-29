package com.llm.eval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDTO {
    
    private Integer evaluationId;
    private Integer llmAnswerId;
    private Integer standardAnswerId;
    private BigDecimal score;
    private String method;
    private String keyPointsEvaluation;
    private Integer judgeModelId;
    private Integer batchId;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 用于关键点评测结果展示
    private List<EvaluationKeyPointDTO> keyPoints;
    
    // 用于展示详情，不作为请求参数
    private StandardAnswerDTO standardAnswer;
    private LlmAnswerDTO llmAnswer;
} 