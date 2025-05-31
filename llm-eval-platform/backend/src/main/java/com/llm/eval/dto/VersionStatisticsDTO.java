package com.llm.eval.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VersionStatisticsDTO {
    
    private Long totalDatasetVersions;
    private Long publishedDatasetVersions;
    private Long totalQuestionVersions;
    private Long questionsWithMultipleVersions;
    private String latestDatasetVersion;
    private Long versionChangesThisMonth;
    private Long versionChangesToday;
}
