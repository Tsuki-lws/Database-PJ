package com.llm.eval.dto;

import com.llm.eval.model.Tag;
import lombok.Data;

@Data
public class TagDTO {
    
    private Integer tagId;
    private String tagName;
    private String description;
    
    public static TagDTO fromEntity(Tag entity) {
        if (entity == null) {
            return null;
        }
        
        TagDTO dto = new TagDTO();
        dto.setTagId(entity.getTagId());
        dto.setTagName(entity.getTagName());
        dto.setDescription(entity.getDescription());
        
        return dto;
    }
} 