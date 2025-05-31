package com.llm.eval.dto;

import lombok.Data;
import java.util.List;

@Data
public class StandardQAImportDTO {
    private Integer qaId;
    private Integer sourceQuestionId;
    private Integer sourceAnswerId;
    private String question;
    private String answer;
    private List<String> keyPoints;
} 