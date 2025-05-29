package com.llm.eval.service.impl;

import com.llm.eval.model.CrowdsourcingAnswer;
import com.llm.eval.model.CrowdsourcingTask;
import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.repository.CrowdsourcingAnswerRepository;
import com.llm.eval.repository.CrowdsourcingTaskRepository;
import com.llm.eval.repository.StandardAnswerRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.service.CrowdsourcingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 众包服务实现类
 */
@Service
public class CrowdsourcingServiceImpl implements CrowdsourcingService {

    private final CrowdsourcingTaskRepository taskRepository;
    private final CrowdsourcingAnswerRepository answerRepository;
    private final StandardQuestionRepository questionRepository;
    private final StandardAnswerRepository standardAnswerRepository;

    @Autowired
    public CrowdsourcingServiceImpl(
            CrowdsourcingTaskRepository taskRepository,
            CrowdsourcingAnswerRepository answerRepository,
            StandardQuestionRepository questionRepository,
            StandardAnswerRepository standardAnswerRepository) {
        this.taskRepository = taskRepository;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.standardAnswerRepository = standardAnswerRepository;
    }

    // 任务管理方法
    @Override
    public List<CrowdsourcingTask> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Optional<CrowdsourcingTask> getTaskById(Integer id) {
        return taskRepository.findById(id);
    }

    @Override
    @Transactional
    public CrowdsourcingTask createTask(CrowdsourcingTask task) {
        // 设置默认状态和时间
        task.setStatus(CrowdsourcingTask.TaskStatus.DRAFT);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setReceivedAnswers(0);
        task.setApprovedAnswers(0);
        
        // 验证问题是否存在
        if (!questionRepository.existsById(task.getQuestionId())) {
            throw new IllegalArgumentException("问题不存在: " + task.getQuestionId());
        }
        
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public CrowdsourcingTask updateTask(Integer id, CrowdsourcingTask task) {
        CrowdsourcingTask existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        // 已发布的任务只能更新部分字段
        if (existingTask.getStatus() != CrowdsourcingTask.TaskStatus.DRAFT) {
            // 只更新描述和所需答案数量
            existingTask.setDescription(task.getDescription());
            existingTask.setRequiredAnswers(task.getRequiredAnswers());
        } else {
            // 草稿状态可以更新所有字段
            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setRequiredAnswers(task.getRequiredAnswers());
            
            // 如果更改了问题ID，需要验证问题是否存在
            if (!existingTask.getQuestionId().equals(task.getQuestionId())) {
                if (!questionRepository.existsById(task.getQuestionId())) {
                    throw new IllegalArgumentException("问题不存在: " + task.getQuestionId());
                }
                existingTask.setQuestionId(task.getQuestionId());
            }
        }
        
        existingTask.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(existingTask);
    }

    @Override
    @Transactional
    public void deleteTask(Integer id) {
        CrowdsourcingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        // 只能删除草稿状态的任务
        if (task.getStatus() != CrowdsourcingTask.TaskStatus.DRAFT) {
            throw new IllegalStateException("只能删除草稿状态的任务");
        }
        
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CrowdsourcingTask publishTask(Integer id) {
        CrowdsourcingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        // 只能发布草稿状态的任务
        if (task.getStatus() != CrowdsourcingTask.TaskStatus.DRAFT) {
            throw new IllegalStateException("只能发布草稿状态的任务");
        }
        
        task.setStatus(CrowdsourcingTask.TaskStatus.PUBLISHED);
        task.setPublishedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public CrowdsourcingTask completeTask(Integer id) {
        CrowdsourcingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        
        // 只能完成已发布的任务
        if (task.getStatus() != CrowdsourcingTask.TaskStatus.PUBLISHED) {
            throw new IllegalStateException("只能完成已发布的任务");
        }
        
        task.setStatus(CrowdsourcingTask.TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        return taskRepository.save(task);
    }

    // 答案管理方法
    @Override
    public List<CrowdsourcingAnswer> getAllAnswers() {
        return answerRepository.findAll();
    }

    @Override
    public Optional<CrowdsourcingAnswer> getAnswerById(Integer id) {
        return answerRepository.findById(id);
    }

    @Override
    public List<CrowdsourcingAnswer> getAnswersByTaskId(Integer taskId) {
        return answerRepository.findByTaskId(taskId);
    }

    @Override
    @Transactional
    public CrowdsourcingAnswer submitAnswer(CrowdsourcingAnswer answer) {
        // 验证任务是否存在并且处于已发布状态
        CrowdsourcingTask task = taskRepository.findById(answer.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + answer.getTaskId()));
        
        if (task.getStatus() != CrowdsourcingTask.TaskStatus.PUBLISHED) {
            throw new IllegalStateException("只能为已发布的任务提交答案");
        }
        
        // 设置默认值
        answer.setCreatedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());
        answer.setReviewStatus(CrowdsourcingAnswer.ReviewStatus.PENDING);
        answer.setPromotedToStandard(false);
        
        // 保存答案
        CrowdsourcingAnswer savedAnswer = answerRepository.save(answer);
        
        // 更新任务的已收到答案数量
        task.setReceivedAnswers(task.getReceivedAnswers() + 1);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        return savedAnswer;
    }

    @Override
    @Transactional
    public CrowdsourcingAnswer updateAnswer(Integer id, CrowdsourcingAnswer answer) {
        CrowdsourcingAnswer existingAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("答案不存在: " + id));
        
        // 只能更新未审核的答案
        if (existingAnswer.getReviewStatus() != CrowdsourcingAnswer.ReviewStatus.PENDING) {
            throw new IllegalStateException("只能更新未审核的答案");
        }
        
        existingAnswer.setContent(answer.getContent());
        existingAnswer.setUpdatedAt(LocalDateTime.now());
        
        return answerRepository.save(existingAnswer);
    }

    @Override
    @Transactional
    public void deleteAnswer(Integer id) {
        CrowdsourcingAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("答案不存在: " + id));
        
        // 只能删除未审核的答案
        if (answer.getReviewStatus() != CrowdsourcingAnswer.ReviewStatus.PENDING) {
            throw new IllegalStateException("只能删除未审核的答案");
        }
        
        // 更新任务的已收到答案数量
        CrowdsourcingTask task = taskRepository.findById(answer.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + answer.getTaskId()));
        
        task.setReceivedAnswers(task.getReceivedAnswers() - 1);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        // 删除答案
        answerRepository.deleteById(id);
    }

    // 审核相关方法
    @Override
    @Transactional
    public CrowdsourcingAnswer reviewAnswer(Integer id, Boolean approved, String comment) {
        CrowdsourcingAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("答案不存在: " + id));
        
        // 只能审核未审核的答案
        if (answer.getReviewStatus() != CrowdsourcingAnswer.ReviewStatus.PENDING) {
            throw new IllegalStateException("答案已经被审核");
        }
        
        // 设置审核结果
        answer.setReviewStatus(approved ? 
                CrowdsourcingAnswer.ReviewStatus.APPROVED : 
                CrowdsourcingAnswer.ReviewStatus.REJECTED);
        answer.setReviewComment(comment);
        answer.setReviewedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());
        
        // 如果通过审核，更新任务的已通过审核答案数量
        if (approved) {
            CrowdsourcingTask task = taskRepository.findById(answer.getTaskId())
                    .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + answer.getTaskId()));
            
            task.setApprovedAnswers(task.getApprovedAnswers() + 1);
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);
        }
        
        return answerRepository.save(answer);
    }

    @Override
    @Transactional
    public StandardAnswer promoteToStandardAnswer(Integer id) {
        CrowdsourcingAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("答案不存在: " + id));
        
        // 只能提升已通过审核的答案
        if (answer.getReviewStatus() != CrowdsourcingAnswer.ReviewStatus.APPROVED) {
            throw new IllegalStateException("只能提升已通过审核的答案");
        }
        
        // 如果已经提升过，不能重复提升
        if (answer.getPromotedToStandard()) {
            throw new IllegalStateException("答案已经被提升为标准答案");
        }
        
        // 获取关联的问题
        CrowdsourcingTask task = taskRepository.findById(answer.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + answer.getTaskId()));
        
        StandardQuestion question = questionRepository.findById(task.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("问题不存在: " + task.getQuestionId()));
        
        // 创建标准答案
        StandardAnswer standardAnswer = new StandardAnswer();
        standardAnswer.setStandardQuestion(question);
        standardAnswer.setAnswer(answer.getContent());
        standardAnswer.setSourceType(StandardAnswer.SourceType.crowdsourced);
        standardAnswer.setSourceId(answer.getId());
        standardAnswer.setSelectionReason("众包答案 #" + answer.getId() + " 提升为标准答案");
        standardAnswer.setIsFinal(false);
        standardAnswer.setCreatedAt(LocalDateTime.now());
        standardAnswer.setUpdatedAt(LocalDateTime.now());
        standardAnswer.setVersion(1);
        
        StandardAnswer savedStandardAnswer = standardAnswerRepository.save(standardAnswer);
        
        // 更新众包答案状态
        answer.setPromotedToStandard(true);
        answer.setPromotedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());
        answerRepository.save(answer);
        
        return savedStandardAnswer;
    }
} 