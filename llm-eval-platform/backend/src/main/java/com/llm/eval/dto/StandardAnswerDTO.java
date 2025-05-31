package com.llm.eval.dto;

import com.llm.eval.model.StandardAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 导入标准答案的DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardAnswerDTO {
    
    /**
     * 关联的标准问题ID
     */
    private Integer standardAnswerId;
    
    /**
     * 关联的标准问题ID
     */
    private Integer standardQuestionId;
    
    /**
     * 标准答案内容
     */
    private String answer;
    
    /**
     * 关联的原始回答ID
     */
    private Integer sourceAnswerId;
    
    /**
     * 标准答案来源类型
     */
    private String sourceType;
    
    /**
     * 关键点列表
     */
    private List<KeyPointDTO> keyPoints;
    
    /**
     * 内部类：关键点DTO
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KeyPointDTO {
        
        /**
         * 关键点描述
         */
        private String pointText;
        
        /**
         * 评分权重
         */
        private BigDecimal pointWeight;
        
        /**
         * 评分点类型
         */
        private String pointType;
        
        /**
         * 示例内容
         */
        private String exampleText;
    }
    
    public static StandardAnswerDTO fromEntity(StandardAnswer entity) {
        if (entity == null) {
            return null;
        }
        
        StandardAnswerDTO dto = new StandardAnswerDTO();
        dto.setStandardAnswerId(entity.getStandardAnswerId());
        
        if (entity.getStandardQuestion() != null) {
            dto.setStandardQuestionId(entity.getStandardQuestion().getStandardQuestionId());
        }
        
        dto.setAnswer(entity.getAnswer());
        
        if (entity.getSourceAnswer() != null) {
            dto.setSourceAnswerId(entity.getSourceAnswer().getAnswerId());
        }
        
        dto.setSourceType(entity.getSourceType().toString());
        
        if (entity.getKeyPoints() != null) {
            dto.setKeyPoints(entity.getKeyPoints().stream()
                    .map(point -> new KeyPointDTO(
                            point.getPointText(), 
                            point.getPointWeight(), 
                            point.getPointType().toString(), 
                            point.getExampleText()))
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
} 