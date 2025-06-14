package com.llm.eval.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 众包任务实体类
 */
@Entity
@Data
@Table(name = "crowdsourcing_tasks")
public class CrowdsourcingTask {
    
    /**
     * 任务ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;
    
    /**
     * 任务标题
     */
    @NotEmpty(message = "任务标题不能为空")
    @Column(nullable = false)
    private String title;
    
    /**
     * 任务描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 创建人ID
     */
    @Column(name = "creator_id", nullable = false)
    private Integer createdBy;
    
    /**
     * 任务状态：DRAFT(草稿), PUBLISHED(已发布), COMPLETED(已完成), CLOSED(已关闭)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.DRAFT;
    
    /**
     * 要求的答案数量
     */
    @Column(name = "min_answers_per_question")
    private Integer requiredAnswers = 3;
    
    /**
     * 当前通过审核的答案数量
     */
    @Column(name = "question_count")
    private Integer currentAnswers = 0;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 标准问题ID（非持久化字段，仅用于前端传递数据）
     */
    @Transient
    private Integer standardQuestionId;
    
    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        DRAFT,      // 草稿状态
        PUBLISHED,  // 已发布，可接受答案
        COMPLETED,  // 已完成，不再接受答案
        CLOSED      // 已关闭，任务结束
    }
} 