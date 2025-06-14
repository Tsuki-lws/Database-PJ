package com.llm.eval.service;

import com.llm.eval.model.CrowdsourcingAnswer;
import com.llm.eval.model.CrowdsourcingTask;
import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.CrowdsourcingTaskQuestion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 众包服务接口
 * 用于管理众包任务和答案
 */
public interface CrowdsourcingService {
    
    // 任务管理
    /**
     * 获取所有众包任务
     * 
     * @return 任务列表
     */
    List<CrowdsourcingTask> getAllTasks();
    
    /**
     * 分页获取众包任务
     * 
     * @param pageable 分页参数
     * @param status 任务状态（可选）
     * @return 分页任务列表
     */
    Page<CrowdsourcingTask> getPagedTasks(Pageable pageable, String status);
    
    /**
     * 根据ID获取众包任务
     * 
     * @param id 任务ID
     * @return 任务对象（如果存在）
     */
    Optional<CrowdsourcingTask> getTaskById(Integer id);
    
    /**
     * 创建众包任务
     * 如果传入了standardQuestionId，会同时创建任务与标准问题的关联
     * 
     * @param task 任务对象
     * @return 创建后的任务对象
     */
    CrowdsourcingTask createTask(CrowdsourcingTask task);
    
    /**
     * 更新众包任务
     * 
     * @param id 任务ID
     * @param task 更新的任务对象
     * @return 更新后的任务对象
     */
    CrowdsourcingTask updateTask(Integer id, CrowdsourcingTask task);
    
    /**
     * 删除众包任务
     * 
     * @param id 任务ID
     */
    void deleteTask(Integer id);
    
    /**
     * 发布众包任务
     * 
     * @param id 任务ID
     * @return 发布后的任务对象
     */
    CrowdsourcingTask publishTask(Integer id);
    
    /**
     * 完成众包任务
     * 
     * @param id 任务ID
     * @return 完成后的任务对象
     */
    CrowdsourcingTask completeTask(Integer id);
    
    /**
     * 获取任务关联的问题
     * 
     * @param taskId 任务ID
     * @return 关联问题列表
     */
    List<CrowdsourcingTaskQuestion> getTaskQuestions(Integer taskId);
    
    // 答案管理
    /**
     * 获取所有众包答案
     * 
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> getAllAnswers();
    
    /**
     * 获取特定状态的所有众包答案
     * 
     * @param status 答案状态
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> getAnswersByStatus(CrowdsourcingAnswer.AnswerStatus status);
    
    /**
     * 根据ID获取众包答案
     * 
     * @param id 答案ID
     * @return 答案对象（如果存在）
     */
    Optional<CrowdsourcingAnswer> getAnswerById(Integer id);
    
    /**
     * 获取特定任务的所有答案
     * 
     * @param taskId 任务ID
     * @return 答案列表
     */
    List<CrowdsourcingAnswer> getAnswersByTaskId(Integer taskId);
    
    /**
     * 提交众包答案
     * 
     * @param answer 答案对象
     * @return 提交后的答案对象
     */
    CrowdsourcingAnswer submitAnswer(CrowdsourcingAnswer answer);
    
    /**
     * 更新众包答案
     * 
     * @param id 答案ID
     * @param answer 更新的答案对象
     * @return 更新后的答案对象
     */
    CrowdsourcingAnswer updateAnswer(Integer id, CrowdsourcingAnswer answer);
    
    /**
     * 删除众包答案
     * 
     * @param id 答案ID
     */
    void deleteAnswer(Integer id);
    
    // 审核相关
    /**
     * 审核众包答案
     * 
     * @param id 答案ID
     * @param approved 是否通过审核
     * @param comment 审核评论
     * @param qualityScore 质量评分（可选）
     * @return 审核后的答案对象
     */
    CrowdsourcingAnswer reviewAnswer(Integer id, Boolean approved, String comment, Integer qualityScore);
    
    /**
     * 将众包答案提升为标准答案
     * 
     * @param id 答案ID
     * @return 创建的标准答案对象
     */
    StandardAnswer promoteToStandardAnswer(Integer id);
} 