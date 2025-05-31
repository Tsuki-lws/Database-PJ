package com.llm.eval.dto;

import lombok.Data;

@Data
public class RawAnswerImportDTO {
    private Integer sourceAnswerId;
    private String answerBody;
    private String authorInfo;
    private Integer upvotes;
    private Boolean isAccepted;
} 