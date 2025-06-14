package com.llm.eval.repository;

import com.llm.eval.model.CrowdsourcingAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * 根据任务ID查询答案（分页版本）
     * 
     * @param taskId 任务ID
     * @param pageable 分页请求
     * @return 答案分页列表
     */
    Page<CrowdsourcingAnswer> findByTaskId(Integer taskId, Pageable pageable);
    
    /**
     * 根据任务ID和审核状态查询答案
     * 
     * @param taskId 任务ID
     * @param status 答案状态
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> findByTaskIdAndStatus(Integer taskId, CrowdsourcingAnswer.AnswerStatus status);
    
    /**
     * 根据任务ID和审核状态查询答案（分页版本）
     * 
     * @param taskId 任务ID
     * @param status 答案状态
     * @param pageable 分页请求
     * @return 答案分页列表
     */
    Page<CrowdsourcingAnswer> findByTaskIdAndStatus(Integer taskId, CrowdsourcingAnswer.AnswerStatus status, Pageable pageable);
    
    /**
     * 根据状态查询答案
     * 
     * @param status 答案状态
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> findByStatus(CrowdsourcingAnswer.AnswerStatus status);
    
    /**
     * 根据贡献者姓名查询答案
     * 
     * @param contributorName 贡献者姓名
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> findByContributorName(String contributorName);
    
    /**
     * 统计任务的答案总数
     * 
     * @param taskId 任务ID
     * @return 答案数量
     */
    long countByTaskId(Integer taskId);
    
    /**
     * 统计特定状态的答案数量
     * 
     * @param status 答案状态
     * @return 答案数量
     */
    long countByStatus(CrowdsourcingAnswer.AnswerStatus status);
    
    /**
     * 统计特定任务下特定状态的答案数量
     * 
     * @param taskId 任务ID
     * @param status 答案状态
     * @return 答案数量
     */
    long countByTaskIdAndStatus(Integer taskId, CrowdsourcingAnswer.AnswerStatus status);
} 