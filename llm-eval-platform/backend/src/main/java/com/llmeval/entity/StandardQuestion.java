package com.llmeval.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("standard_questions")
public class StandardQuestion {
    @TableId(type = IdType.AUTO)
    private Long standardQuestionId;
    
    private Long originalQuestionId;
    
    private Integer version;
    
    private String content;
    
    private String questionType;
    
    private String difficultyLevel;
    
    private Long categoryId;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private Long createdBy;
    
    private Boolean isLatest;
    
    private Long parentQuestionId;
    
    @TableLogic
    private Integer deleted;
} 