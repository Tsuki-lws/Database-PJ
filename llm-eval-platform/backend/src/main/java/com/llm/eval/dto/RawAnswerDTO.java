package com.llm.eval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入原始回答的DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RawAnswerDTO {
    
    /**
     * 关联的原始问题ID
     */
    private Integer questionId;
    
    /**
     * 来源平台的回答ID
     */
    private Integer sourceAnswerId;
    
    /**
     * 回答内容(HTML格式)
     */
    private String answerBody;
    
    /**
     * 回答者信息
     */
    private String authorInfo;
    
    /**
     * 点赞数量
     */
    private Integer upvotes;
    
    /**
     * 是否为采纳的答案
     */
    private Boolean isAccepted;
} 