package com.llm.eval.repository;

import com.llm.eval.model.CrowdsourcingTaskQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 众包任务-问题关联数据访问接口
 */
@Repository
public interface CrowdsourcingTaskQuestionRepository extends JpaRepository<CrowdsourcingTaskQuestion, Integer> {
    
    /**
     * 根据任务ID查询关联问题
     * 
     * @param taskId 任务ID
     * @return 关联问题列表
     */
    List<CrowdsourcingTaskQuestion> findByTaskId(Integer taskId);
    
    /**
     * 根据标准问题ID查询关联任务
     * 
     * @param standardQuestionId 标准问题ID
     * @return 关联任务列表
     */
    List<CrowdsourcingTaskQuestion> findByStandardQuestionId(Integer standardQuestionId);
    
    /**
     * 根据任务ID和标准问题ID查询关联
     * 
     * @param taskId 任务ID
     * @param standardQuestionId 标准问题ID
     * @return 关联对象
     */
    CrowdsourcingTaskQuestion findByTaskIdAndStandardQuestionId(Integer taskId, Integer standardQuestionId);
    
    /**
     * 删除任务的所有关联问题
     * 
     * @param taskId 任务ID
     */
    void deleteByTaskId(Integer taskId);
} 