package com.llm.eval.dto;

import lombok.Data;

import java.util.List;

/**
 * 原始问题转换为标准问题的参数DTO
 */
@Data
public class RawQuestionConverter {
    
    /**
     * 分类ID
     */
    private Integer categoryId;
    
    /**
     * 问题类型：单选题、多选题、简单事实题、主观题
     */
    private String questionType;
    
    /**
     * 问题难度：简单、中等、困难
     */
    private String difficulty;
    
    /**
     * 标签ID列表
     */
    private List<Integer> tagIds;
    
    /**
     * 自定义标准问题文本，为空则使用原始问题文本
     */
    private String customQuestion;
    
    /**
     * 是否同时转换原始回答为标准答案
     */
    private Boolean convertAnswers = false;
    
    /**
     * 选择哪个原始回答ID作为标准答案
     */
    private Integer selectedAnswerId;
} 