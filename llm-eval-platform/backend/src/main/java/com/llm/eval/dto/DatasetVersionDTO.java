package com.llm.eval.dto;

import com.llm.eval.model.DatasetVersion;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DatasetVersionDTO {
    
    private Integer versionId;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Boolean isPublished;
    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Integer> questionIds;
    
    public static DatasetVersionDTO fromEntity(DatasetVersion entity) {
        return fromEntity(entity, false);
    }
    
    public static DatasetVersionDTO fromEntity(DatasetVersion entity, boolean includeQuestionIds) {
        if (entity == null) {
            return null;
        }
        
        DatasetVersionDTO dto = new DatasetVersionDTO();
        dto.setVersionId(entity.getVersionId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setReleaseDate(entity.getReleaseDate());
        dto.setIsPublished(entity.getIsPublished());
        dto.setQuestionCount(entity.getQuestionCount());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        if (includeQuestionIds && entity.getQuestions() != null) {
            dto.setQuestionIds(entity.getQuestions().stream()
                    .map(q -> q.getStandardQuestionId())
                    .collect(Collectors.toSet()));
        }
        
        return dto;
    }
} 