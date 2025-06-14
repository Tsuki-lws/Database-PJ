package com.llm.eval.repository;

import com.llm.eval.model.CrowdsourcingTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<CrowdsourcingTask> findByStatus(CrowdsourcingTask.TaskStatus status);
    
    /**
     * 分页查询任务
     * 
     * @param pageable 分页参数
     * @return 分页任务列表
     */
    Page<CrowdsourcingTask> findAll(Pageable pageable);
    
    /**
     * 根据状态分页查询任务
     * 
     * @param status 任务状态
     * @param pageable 分页参数
     * @return 分页任务列表
     */
    Page<CrowdsourcingTask> findByStatus(CrowdsourcingTask.TaskStatus status, Pageable pageable);
    
    /**
     * 根据创建者查询任务
     * 
     * @param createdBy 创建者ID
     * @return 任务列表
     */
    List<CrowdsourcingTask> findByCreatedBy(Integer createdBy);
} 