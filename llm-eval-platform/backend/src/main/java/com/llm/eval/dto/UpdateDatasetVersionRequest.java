package com.llm.eval.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDatasetVersionRequest {
    
    private String versionName;
    private String description;
    private LocalDate releaseDate;
    private List<Integer> questionIds; // 要更新的问题ID列表
    private Boolean isPublished; // 是否发布状态
}
