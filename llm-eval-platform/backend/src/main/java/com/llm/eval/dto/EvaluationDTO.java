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
    
    /**
     * 获取模型ID
     * @return 如果llmAnswer不为空且其model不为空，则返回model的ID，否则返回null
     */
    public Integer getModelId() {
        if (llmAnswer != null && llmAnswer.getModel() != null) {
            return llmAnswer.getModel().getModelId();
        }
        return null;
    }
    
    /**
     * 获取问题内容
     * @return 如果llmAnswer不为空且其standardQuestion不为空，则返回问题内容，否则返回null
     */
    public String getQuestion() {
        if (llmAnswer != null && llmAnswer.getStandardQuestion() != null) {
            return llmAnswer.getStandardQuestion().getQuestion();
        }
        return null;
    }
    
    /**
     * 获取模型名称
     * @return 如果llmAnswer不为空且其model不为空，则返回模型名称，否则返回null
     */
    public String getModelName() {
        if (llmAnswer != null && llmAnswer.getModel() != null) {
            String name = llmAnswer.getModel().getName();
            String version = llmAnswer.getModel().getVersion();
            if (version != null && !version.isEmpty()) {
                return name + " " + version;
            }
            return name;
        }
        return null;
    }
} 