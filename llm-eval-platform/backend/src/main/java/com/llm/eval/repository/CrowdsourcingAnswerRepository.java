package com.llm.eval.repository;

import com.llm.eval.model.CrowdsourcingAnswer;
import com.llm.eval.model.CrowdsourcingAnswer.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 众包答案数据访问接口
 */
@Repository
public interface CrowdsourcingAnswerRepository extends JpaRepository<CrowdsourcingAnswer, Integer> {
    
    /**
     * 根据任务ID查询答案
     * 
     * @param taskId 任务ID
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> findByTaskId(Integer taskId);
    
    /**
     * 根据任务ID和审核状态查询答案
     * 
     * @param taskId 任务ID
     * @param reviewStatus 审核状态
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> findByTaskIdAndReviewStatus(Integer taskId, ReviewStatus reviewStatus);
    
    /**
     * 统计特定审核状态的答案数量
     * 
     * @param reviewStatus 审核状态
     * @return 答案数量
     */
    long countByReviewStatus(ReviewStatus reviewStatus);
    
    /**
     * 查询某个任务下特定审核状态的答案数量
     * 
     * @param taskId 任务ID
     * @param reviewStatus 审核状态
     * @return 答案数量
     */
    long countByTaskIdAndReviewStatus(Integer taskId, ReviewStatus reviewStatus);
    
    /**
     * 查询是否已经提升为标准答案的答案列表
     * 
     * @param promotedToStandard 是否已提升为标准答案
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> findByPromotedToStandard(Boolean promotedToStandard);
} 