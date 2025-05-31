package com.llm.eval.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionVersionRequest {
    
    private String versionName;
    private String changeReason;
    private String questionTitle;
    private String questionBody;
    private String standardAnswer;
    private List<String> referenceAnswers;
    private Integer categoryId;
    private String questionType;
    private String difficulty;
}
