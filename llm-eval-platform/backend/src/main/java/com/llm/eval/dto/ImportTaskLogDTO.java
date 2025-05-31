package com.llm.eval.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ImportTaskLogDTO {
    private Integer logId;
    private Integer importId;
    private LocalDateTime logTime;
    private String message;
    private String logLevel;
} 