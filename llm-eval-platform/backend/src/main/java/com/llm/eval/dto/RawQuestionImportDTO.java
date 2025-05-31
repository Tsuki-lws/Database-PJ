package com.llm.eval.dto;

import lombok.Data;
import java.util.List;

@Data
public class RawQuestionImportDTO {
    private Integer sourceQuestionId;
    private String questionTitle;
    private String questionBody;
    private String source;
    private Integer sourceId;
    private String sourceUrl;
    private RawAnswerImportDTO answer;
} 