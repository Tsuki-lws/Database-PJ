package com.llm.eval.service.impl;

import com.llm.eval.model.CrowdsourcingAnswer;
import com.llm.eval.model.CrowdsourcingTask;
import com.llm.eval.model.CrowdsourcingTaskQuestion;
import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.repository.CrowdsourcingAnswerRepository;
import com.llm.eval.repository.CrowdsourcingTaskQuestionRepository;
import com.llm.eval.repository.CrowdsourcingTaskRepository;
import com.llm.eval.repository.StandardAnswerRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.service.CrowdsourcingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    private final StandardQuestionRepository standardQuestionRepository;
    private final StandardAnswerRepository standardAnswerRepository;
    private final CrowdsourcingTaskQuestionRepository taskQuestionRepository;

    @Autowired
    public CrowdsourcingServiceImpl(
            CrowdsourcingTaskRepository taskRepository,
            CrowdsourcingAnswerRepository answerRepository,
            StandardQuestionRepository standardQuestionRepository,
            StandardAnswerRepository standardAnswerRepository,
            CrowdsourcingTaskQuestionRepository taskQuestionRepository) {
        this.taskRepository = taskRepository;
        this.answerRepository = answerRepository;
        this.standardQuestionRepository = standardQuestionRepository;
        this.standardAnswerRepository = standardAnswerRepository;
        this.taskQuestionRepository = taskQuestionRepository;
    }

    // 任务管理方法
    @Override
    public List<CrowdsourcingTask> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Page<CrowdsourcingTask> getPagedTasks(Pageable pageable, String status) {
        if (status != null && !status.isEmpty()) {
            try {
                // 将状态转换为大写，以匹配枚举值
                String upperCaseStatus = status.toUpperCase();
                System.out.println("服务层 - 尝试转换状态参数: [" + status + "] -> [" + upperCaseStatus + "]");
                
                // 验证状态值是否有效
                CrowdsourcingTask.TaskStatus taskStatus = CrowdsourcingTask.TaskStatus.valueOf(upperCaseStatus);
                System.out.println("服务层 - 状态参数有效: [" + upperCaseStatus + "]");
                
                // 使用转换后的大写状态值进行查询
                Page<CrowdsourcingTask> result = taskRepository.findByStatus(taskStatus, pageable);
                System.out.println("服务层 - 按状态[" + upperCaseStatus + "]查询，找到[" + result.getTotalElements() + "]条记录");
                
                // 打印找到的任务状态
                if (result.hasContent()) {
                    System.out.println("服务层 - 找到的任务状态列表:");
                    for (CrowdsourcingTask task : result.getContent()) {
                        System.out.println("服务层 - 任务ID: " + task.getTaskId() + ", 状态: [" + task.getStatus() + "]");
                    }
                } else {
                    System.out.println("服务层 - 未找到任务，尝试直接执行SQL查询检查数据库中的状态值");
                    // 这里可以添加直接执行SQL查询的代码，但这需要EntityManager注入
                }
                
                return result;
            } catch (IllegalArgumentException e) {
                System.err.println("服务层 - 无效的状态值: [" + status + "], 查询所有状态");
                return taskRepository.findAll(pageable);
            }
        }
        System.out.println("服务层 - 状态参数为空，查询所有状态");
        Page<CrowdsourcingTask> result = taskRepository.findAll(pageable);
        
        // 打印找到的所有任务状态
        if (result.hasContent()) {
            System.out.println("服务层 - 找到的所有任务状态列表:");
            for (CrowdsourcingTask task : result.getContent()) {
                System.out.println("服务层 - 任务ID: " + task.getTaskId() + ", 状态: [" + task.getStatus() + "]");
            }
        }
        
        return result;
    }

    @Override
    public Optional<CrowdsourcingTask> getTaskById(Integer id) {
        return taskRepository.findById(id);
    }

    @Override
    @Transactional
    public CrowdsourcingTask createTask(CrowdsourcingTask task) {
        // 验证标准问题是否存在
        if (task.getStandardQuestionId() != null) {
            standardQuestionRepository.findById(task.getStandardQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("标准问题不存在，ID: " + task.getStandardQuestionId()));
        } else {
            throw new IllegalArgumentException("创建任务时必须指定标准问题ID");
        }
        
        // 设置初始状态
        task.setStatus(CrowdsourcingTask.TaskStatus.DRAFT);
        task.setCurrentAnswers(0);
        
        // 保存任务
        CrowdsourcingTask savedTask = taskRepository.save(task);
        
        // 创建任务-问题关联
        CrowdsourcingTaskQuestion taskQuestion = new CrowdsourcingTaskQuestion();
        taskQuestion.setTaskId(savedTask.getTaskId());
        taskQuestion.setStandardQuestionId(task.getStandardQuestionId());
        taskQuestion.setCurrentAnswerCount(0);
        taskQuestion.setIsCompleted(false);
        
        taskQuestionRepository.save(taskQuestion);
        
        return savedTask;
    }

    @Override
    @Transactional
    public CrowdsourcingTask updateTask(Integer id, CrowdsourcingTask task) {
        CrowdsourcingTask existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("众包任务不存在，ID: " + id));
        
        // 只允许在草稿状态下修改关键属性
        if (existingTask.getStatus() != CrowdsourcingTask.TaskStatus.DRAFT) {
            if (existingTask.getRequiredAnswers() < task.getRequiredAnswers()) {
                throw new IllegalStateException("已发布的任务只能减少所需答案数量，不能增加");
            }
        }
        
        // 更新属性
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setRequiredAnswers(task.getRequiredAnswers());
        
        return taskRepository.save(existingTask);
    }

    @Override
    @Transactional
    public void deleteTask(Integer id) {
        CrowdsourcingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("众包任务不存在，ID: " + id));
        
        // 如果任务已发布且有答案，不允许删除
        if (task.getStatus() != CrowdsourcingTask.TaskStatus.DRAFT && task.getCurrentAnswers() > 0) {
            throw new IllegalStateException("已有答案的任务不能删除，请考虑关闭任务");
        }
        
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CrowdsourcingTask publishTask(Integer id) {
        CrowdsourcingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("众包任务不存在，ID: " + id));
        
        // 只能发布草稿状态的任务
        if (task.getStatus() != CrowdsourcingTask.TaskStatus.DRAFT) {
            throw new IllegalStateException("只能发布草稿状态的任务");
        }
        
        task.setStatus(CrowdsourcingTask.TaskStatus.PUBLISHED);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public CrowdsourcingTask completeTask(Integer id) {
        CrowdsourcingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("众包任务不存在，ID: " + id));
        
        // 只能完成已发布的任务
        if (task.getStatus() != CrowdsourcingTask.TaskStatus.PUBLISHED) {
            throw new IllegalStateException("只能完成已发布的任务");
        }
        
        task.setStatus(CrowdsourcingTask.TaskStatus.COMPLETED);
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
        System.out.println("Service层处理提交答案: " + answer);
        
        if (answer == null) {
            throw new IllegalArgumentException("答案对象不能为null");
        }
        
        if (answer.getTaskId() == null) {
            throw new IllegalArgumentException("任务ID不能为null");
        }
        
        // 验证任务是否存在
        try {
            CrowdsourcingTask task = taskRepository.findById(answer.getTaskId())
                    .orElseThrow(() -> new EntityNotFoundException("众包任务不存在，ID: " + answer.getTaskId()));
            
            // 验证任务状态
            if (task.getStatus() != CrowdsourcingTask.TaskStatus.PUBLISHED) {
                throw new IllegalStateException("只能为已发布的任务提交答案");
            }
            
            // 设置初始状态
            answer.setStatus(CrowdsourcingAnswer.AnswerStatus.SUBMITTED);
            
            // 确保task关联对象为null，避免序列化问题
            answer.setTask(null);
            
            // 检查references字段是否有特殊处理需求
            if (answer.getReferences() != null) {
                // 打印引用字段的实际内容
                System.out.println("引用字段内容: [" + answer.getReferences() + "]");
            }
            
            System.out.println("保存答案前: " + answer);
            
            try {
                CrowdsourcingAnswer savedAnswer = answerRepository.save(answer);
                System.out.println("保存答案成功: " + savedAnswer);
                return savedAnswer;
            } catch (Exception e) {
                System.err.println("保存答案时捕获异常: " + e.getClass().getName() + ": " + e.getMessage());
                
                // 获取并打印根本原因
                Throwable rootCause = e;
                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }
                System.err.println("根本原因: " + rootCause.getClass().getName() + ": " + rootCause.getMessage());
                
                if (rootCause.getMessage() != null && rootCause.getMessage().contains("references")) {
                    System.err.println("似乎是由于'references'字段导致的SQL错误，这是MySQL的保留字");
                }
                
                throw e;
            }
        } catch (Exception e) {
            System.err.println("提交答案时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public CrowdsourcingAnswer updateAnswer(Integer id, CrowdsourcingAnswer answer) {
        CrowdsourcingAnswer existingAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("众包答案不存在，ID: " + id));
        
        // 只允许更新未审核的答案
        if (existingAnswer.getStatus() != CrowdsourcingAnswer.AnswerStatus.SUBMITTED) {
            throw new IllegalStateException("已审核的答案不能修改");
        }
        
        // 不允许修改任务ID
        if (!existingAnswer.getTaskId().equals(answer.getTaskId())) {
            throw new IllegalStateException("不能修改答案所属的任务");
        }
        
        // 更新属性
        existingAnswer.setAnswerText(answer.getAnswerText());
        existingAnswer.setContributorName(answer.getContributorName());
        existingAnswer.setContributorEmail(answer.getContributorEmail());
        
        return answerRepository.save(existingAnswer);
    }

    @Override
    @Transactional
    public void deleteAnswer(Integer id) {
        CrowdsourcingAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("众包答案不存在，ID: " + id));
        
        // 如果答案已审核，不允许删除
        if (answer.getStatus() != CrowdsourcingAnswer.AnswerStatus.SUBMITTED) {
            throw new IllegalStateException("已审核的答案不能删除");
        }
        
        answerRepository.deleteById(id);
    }

    // 审核相关方法
    @Override
    @Transactional
    public CrowdsourcingAnswer reviewAnswer(Integer id, Boolean approved, String comment) {
        CrowdsourcingAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("众包答案不存在，ID: " + id));
        
        // 设置审核状态
        if (approved) {
            answer.setStatus(CrowdsourcingAnswer.AnswerStatus.APPROVED);
            
            // 更新任务的已批准答案数量
            CrowdsourcingTask task = taskRepository.findById(answer.getTaskId())
                    .orElseThrow(() -> new EntityNotFoundException("众包任务不存在"));
            
            task.setCurrentAnswers(task.getCurrentAnswers() + 1);
            
            // 如果达到所需答案数量，自动完成任务
            if (task.getCurrentAnswers() >= task.getRequiredAnswers()) {
                task.setStatus(CrowdsourcingTask.TaskStatus.COMPLETED);
            }
            
            taskRepository.save(task);
        } else {
            answer.setStatus(CrowdsourcingAnswer.AnswerStatus.REJECTED);
        }
        
        // 设置审核信息
        answer.setReviewComment(comment);
        
        return answerRepository.save(answer);
    }

    @Override
    @Transactional
    public StandardAnswer promoteToStandardAnswer(Integer id) {
        // 获取众包答案
        CrowdsourcingAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("众包答案不存在，ID: " + id));
        
        // 验证答案是否已批准
        if (answer.getStatus() != CrowdsourcingAnswer.AnswerStatus.APPROVED) {
            throw new IllegalStateException("只有已批准的众包答案才能提升为标准答案");
        }
        
        // 获取对应的标准问题
        StandardQuestion question = standardQuestionRepository.findById(answer.getStandardQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("标准问题不存在"));
        
        // 创建标准答案
        StandardAnswer standardAnswer = new StandardAnswer();
        // 设置关联的标准问题
        standardAnswer.setStandardQuestion(question);
        standardAnswer.setAnswer(answer.getAnswerText());
        // 设置来源类型和ID
        standardAnswer.setSourceType(StandardAnswer.SourceType.crowdsourced);
        standardAnswer.setSourceId(answer.getAnswerId());
        standardAnswer.setSelectionReason("从众包答案提升");
        standardAnswer.setSelectedBy(1); // 管理员ID，实际应用中应该从当前登录用户获取
        standardAnswer.setIsFinal(false); // 默认不是最终答案，需要手动设置
        
        StandardAnswer savedStandardAnswer = standardAnswerRepository.save(standardAnswer);
        
        // 更新众包答案状态
        answer.setStatus(CrowdsourcingAnswer.AnswerStatus.PROMOTED);
        answerRepository.save(answer);
        
        return savedStandardAnswer;
    }

    // 添加根据创建者ID查询任务的方法
    public List<CrowdsourcingTask> getTasksByCreatedBy(Integer createdBy) {
        return taskRepository.findByCreatedBy(createdBy);
    }

    @Override
    public List<CrowdsourcingTaskQuestion> getTaskQuestions(Integer taskId) {
        // 验证任务是否存在
        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("众包任务不存在，ID: " + taskId);
        }
        
        return taskQuestionRepository.findByTaskId(taskId);
    }
} 