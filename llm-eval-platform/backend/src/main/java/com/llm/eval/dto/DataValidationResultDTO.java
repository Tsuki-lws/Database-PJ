package com.llm.eval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataValidationResultDTO {
    private boolean isValid;
    private int totalRecords;
    private int invalidRecords;
    private List<ValidationIssueDTO> issues = new ArrayList<>();
    private List<ValidationIssueDTO> warnings = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationIssueDTO {
        private int recordIndex;
        private String field;
        private String message;
    }
} 