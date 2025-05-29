/*
package com.llm.eval.dto;

import com.llm.eval.model.EvaluationBatch.EvaluationMethod;
import com.llm.eval.model.EvaluationBatch.EvaluationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationBatchDTO {
    
    private Integer batchId;
    private String name;
    private String description;
    private LlmModelDTO model;
    private LlmModelDTO judgeModel;
    private DatasetVersionDTO datasetVersion;
    private EvaluationMethod evaluationMethod;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EvaluationStatus status;
    private String metricsSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 用于前端展示的统计数据
    private Integer totalQuestions;
    private Integer completedQuestions;
    private Double averageScore;
} 
*/ 