package com.llm.eval.dto;

import com.llm.eval.model.StandardAnswer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class StandardAnswerDTO {
    
    private Integer standardAnswerId;
    private Integer standardQuestionId;
    private String answer;
    private Integer sourceAnswerId;
    private StandardAnswer.SourceType sourceType;
    private Integer sourceId;
    private String selectionReason;
    private Integer selectedBy;
    private Boolean isFinal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private List<KeyPointDTO> keyPoints;
    
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
        
        dto.setSourceType(entity.getSourceType());
        dto.setSourceId(entity.getSourceId());
        dto.setSelectionReason(entity.getSelectionReason());
        dto.setSelectedBy(entity.getSelectedBy());
        dto.setIsFinal(entity.getIsFinal());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setVersion(entity.getVersion());
        
        if (entity.getKeyPoints() != null) {
            dto.setKeyPoints(entity.getKeyPoints().stream()
                    .map(KeyPointDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
} 