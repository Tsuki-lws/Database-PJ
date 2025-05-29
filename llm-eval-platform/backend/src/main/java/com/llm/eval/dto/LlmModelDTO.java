package com.llm.eval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmModelDTO {
    
    private Integer modelId;
    private String name;
    private String version;
    private String provider;
    private String description;
    private String apiConfig;
    private LocalDateTime createdAt;
} 