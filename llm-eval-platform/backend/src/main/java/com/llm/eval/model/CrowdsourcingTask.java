package com.llm.eval.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 众包任务实体类
 */
@Entity
@Table(name = "crowdsourcing_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrowdsourcingTask {
    
    /**
     * 任务ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    @Column(nullable = false)
    private String title;
    
    /**
     * 任务描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 问题ID
     */
    @NotNull(message = "问题ID不能为空")
    @Column(name = "question_id", nullable = false)
    private Integer questionId;
    
    /**
     * 任务状态：DRAFT(草稿), PUBLISHED(已发布), COMPLETED(已完成)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.DRAFT;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 发布时间
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    /**
     * 完成时间
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Integer createdBy;
    
    /**
     * 要求的答案数量
     */
    @Column(name = "required_answers")
    private Integer requiredAnswers = 3;
    
    /**
     * 收到的答案数量
     */
    @Column(name = "received_answers")
    private Integer receivedAnswers = 0;
    
    /**
     * 当前通过审核的答案数量
     */
    @Column(name = "approved_answers")
    private Integer approvedAnswers = 0;
    
    /**
     * 关联的答案集合
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CrowdsourcingAnswer> answers = new HashSet<>();
    
    /**
     * 关联的标准问题
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private StandardQuestion question;
    
    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        COMPLETED   // 已完成
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 