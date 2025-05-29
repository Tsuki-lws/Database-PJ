package com.llm.eval.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 众包答案实体类
 */
@Entity
@Table(name = "crowdsourcing_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrowdsourcingAnswer {
    
    /**
     * 答案ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    @Column(name = "task_id", nullable = false)
    private Integer taskId;
    
    /**
     * 答案内容
     */
    @NotBlank(message = "答案内容不能为空")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * 提交人ID
     */
    @Column(name = "submitted_by")
    private Integer submittedBy;
    
    /**
     * 审核状态：PENDING(待审核), APPROVED(已通过), REJECTED(已拒绝)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;
    
    /**
     * 审核评论
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;
    
    /**
     * 审核人ID
     */
    @Column(name = "reviewed_by")
    private Integer reviewedBy;
    
    /**
     * 审核时间
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
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
     * 是否已提升为标准答案
     */
    @Column(name = "promoted_to_standard")
    private Boolean promotedToStandard = false;
    
    /**
     * 提升为标准答案的时间
     */
    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;
    
    /**
     * 关联的众包任务
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private CrowdsourcingTask task;
    
    /**
     * 审核状态枚举
     */
    public enum ReviewStatus {
        PENDING,   // 待审核
        APPROVED,  // 已通过
        REJECTED   // 已拒绝
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