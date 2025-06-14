package com.llm.eval.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 众包任务-问题关联实体类
 */
@Entity
@Data
@Table(name = "crowdsourcing_task_questions")
public class CrowdsourcingTaskQuestion {
    
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Integer taskId;
    
    /**
     * 关联的众包任务
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private CrowdsourcingTask task;
    
    /**
     * 标准问题ID
     */
    @Column(name = "standard_question_id", nullable = false)
    private Integer standardQuestionId;
    
    /**
     * 关联的标准问题
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_question_id", insertable = false, updatable = false)
    private StandardQuestion standardQuestion;
    
    /**
     * 当前收集的答案数量
     */
    @Column(name = "current_answer_count", nullable = false)
    private Integer currentAnswerCount = 0;
    
    /**
     * 是否已收集足够答案
     */
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 