package com.llm.eval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmAnswerDTO {
    
    private Integer llmAnswerId;
    private LlmModelDTO model;
    private StandardQuestionDTO standardQuestion;
    private DatasetVersionDTO datasetVersion;
    private String content;
    private Integer latency;
    private Integer tokensUsed;
    private String promptTemplate;
    private String promptParams;
    private Double temperature;
    private Double topP;
    private Integer batchId;
    private Integer retryCount;
    private Boolean isFinal;
    private Integer parentAnswerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 可选属性，用于显示评测结果
    private EvaluationDTO evaluation;
} 