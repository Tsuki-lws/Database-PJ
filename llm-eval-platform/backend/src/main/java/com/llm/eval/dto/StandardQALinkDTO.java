package com.llm.eval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标准问答对关联DTO
 * 用于创建标准问题和标准答案之间的关联关系
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardQALinkDTO {
    
    /**
     * 标准问题ID
     */
    private Integer standardQuestionId;
    
    /**
     * 标准答案ID
     */
    private Integer standardAnswerId;
    
    /**
     * 原始问题ID（可选）
     */
    private Integer sourceQuestionId;
    
    /**
     * 原始回答ID（可选）
     */
    private Integer sourceAnswerId;
} 