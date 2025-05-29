package com.llm.eval.dto;

import com.llm.eval.model.AnswerKeyPoint;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KeyPointDTO {
    
    private Integer keyPointId;
    private Integer standardAnswerId;
    private String pointText;
    private Integer pointOrder;
    private BigDecimal pointWeight;
    private AnswerKeyPoint.PointType pointType;
    private String exampleText;
    
    public static KeyPointDTO fromEntity(AnswerKeyPoint entity) {
        if (entity == null) {
            return null;
        }
        
        KeyPointDTO dto = new KeyPointDTO();
        dto.setKeyPointId(entity.getKeyPointId());
        
        if (entity.getStandardAnswer() != null) {
            dto.setStandardAnswerId(entity.getStandardAnswer().getStandardAnswerId());
        }
        
        dto.setPointText(entity.getPointText());
        dto.setPointOrder(entity.getPointOrder());
        dto.setPointWeight(entity.getPointWeight());
        dto.setPointType(entity.getPointType());
        dto.setExampleText(entity.getExampleText());
        
        return dto;
    }
} 