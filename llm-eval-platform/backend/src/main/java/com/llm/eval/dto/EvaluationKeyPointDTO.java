package com.llm.eval.dto;

import com.llm.eval.model.EvaluationKeyPoint.KeyPointStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationKeyPointDTO {
    
    private Integer id;
    private Integer evaluationId;
    private Integer keyPointId;
    private KeyPointStatus status;
    private BigDecimal score;
    private LocalDateTime createdAt;
    
    // 关联展示
    private String keyPointContent;
} 