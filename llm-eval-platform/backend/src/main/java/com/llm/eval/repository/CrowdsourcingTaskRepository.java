package com.llm.eval.repository;

import com.llm.eval.model.CrowdsourcingTask;
import com.llm.eval.model.CrowdsourcingTask.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 众包任务数据访问接口
 */
@Repository
public interface CrowdsourcingTaskRepository extends JpaRepository<CrowdsourcingTask, Integer> {
    
    /**
     * 根据状态查询任务
     * 
     * @param status 任务状态
     * @return 任务列表
     */
    List<CrowdsourcingTask> findByStatus(TaskStatus status);
    
    /**
     * 根据问题ID查询任务
     * 
     * @param questionId 问题ID
     * @return 任务列表
     */
    List<CrowdsourcingTask> findByQuestionId(Integer questionId);
    
    /**
     * 查询某个问题下是否已有进行中的任务
     * 
     * @param questionId 问题ID
     * @param status 任务状态
     * @return 任务数量
     */
    long countByQuestionIdAndStatus(Integer questionId, TaskStatus status);
} 