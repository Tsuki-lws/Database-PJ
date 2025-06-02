package com.llm.eval.dto;

import lombok.Data;

/**
 * 查询参数DTO
 */
@Data
public class QueryParams {
    
    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 排序字段
     */
    private String sortField;
    
    /**
     * 排序方向，asc或desc
     */
    private String sortOrder;
    
    /**
     * 分类ID
     */
    private Integer categoryId;
    
    /**
     * 标签ID
     */
    private Integer tagId;
    
    /**
     * 问题难度
     */
    private String difficulty;
    
    /**
     * 问题类型
     */
    private String questionType;
    
    /**
     * 问题状态
     */
    private String status;
    
    /**
     * 来源
     */
    private String source;
} 