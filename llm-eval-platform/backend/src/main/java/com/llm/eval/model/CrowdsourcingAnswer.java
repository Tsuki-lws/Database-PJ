package com.llm.eval.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 众包答案实体类
 */
@Entity
@Data
@Table(name = "crowdsourced_answers")
public class CrowdsourcingAnswer {
    /**
     * 答案ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Integer answerId;
    
    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
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
    @NotNull(message = "标准问题ID不能为空")
    @Column(name = "standard_question_id", nullable = false)
    private Integer standardQuestionId;
    
    /**
     * 答案内容
     */
    @NotEmpty(message = "答案内容不能为空")
    @Column(name = "answer_text", columnDefinition = "TEXT", nullable = false)
    private String answerText;
    
    /**
     * 提交人名称
     */
    @Column(name = "contributor_name")
    private String contributorName;
    
    /**
     * 提交人邮箱
     */
    @Column(name = "contributor_email")
    private String contributorEmail;
    
    /**
     * 职业/身份
     */
    @Column(name = "occupation")
    private String occupation;
    
    /**
     * 专业领域
     */
    @Column(name = "expertise")
    private String expertise;
    
    /**
     * 参考资料
     */
    @Column(name = "`references`")
    private String references;
    
    /**
     * 审核状态：SUBMITTED(已提交), APPROVED(已通过), REJECTED(已拒绝), PROMOTED(已提升为标准答案)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private AnswerStatus status = AnswerStatus.SUBMITTED;
    
    /**
     * 审核评论
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;
    
    /**
     * 质量评分
     */
    @Column(name = "quality_score", nullable = true)
    private Integer qualityScore;
    
    /**
     * 审核人ID
     */
    @Column(name = "reviewer_id", nullable = true)
    private String reviewedBy;
    
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
     * 审核状态枚举
     */
    public enum AnswerStatus {
        SUBMITTED,  // 已提交
        APPROVED,   // 已批准
        REJECTED,   // 已拒绝
        PROMOTED    // 已提升为标准答案
    }
} 