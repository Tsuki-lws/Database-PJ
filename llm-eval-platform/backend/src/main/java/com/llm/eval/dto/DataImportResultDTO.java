package com.llm.eval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataImportResultDTO {
    private boolean success;
    private String message;
    private int imported;
    private int failed;
    private int total;
} 