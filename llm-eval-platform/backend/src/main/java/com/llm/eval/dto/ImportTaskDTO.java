package com.llm.eval.dto;

import com.llm.eval.model.ImportTask;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ImportTaskDTO {
    private Integer importId;
    private String type;
    private String filename;
    private Integer importedCount;
    private Integer updatedCount;
    private Integer failedCount;
    private String status;
    private String details;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer userId;
    private String userName;
    private List<ImportTaskLogDTO> logs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 