package com.llm.eval.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDatasetVersionRequest {
    
    private String versionName;
    private String description;
    private List<Integer> questionIds;
    private Integer baseVersionId; // 基于哪个版本创建
}
