package com.llm.eval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仅导入原始问题的DTO（不包含回答）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RawQuestionOnlyDTO {
    
    /**
     * 来源平台的问题ID
     */
    private Integer sourceQuestionId;
    
    /**
     * 问题标题
     */
    private String questionTitle;
    
    /**
     * 问题内容(HTML格式)
     */
    private String questionBody;
    
    /**
     * 来源网站
     */
    private String source;
    
    /**
     * 来源网站上的ID
     */
    private Integer sourceId;
    
    /**
     * 来源URL
     */
    private String sourceUrl;
} 