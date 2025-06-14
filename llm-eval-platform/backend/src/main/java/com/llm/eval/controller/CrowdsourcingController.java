package com.llm.eval.controller;

import com.llm.eval.model.CrowdsourcingAnswer;
import com.llm.eval.model.CrowdsourcingTask;
import com.llm.eval.model.CrowdsourcingTaskQuestion;
import com.llm.eval.repository.CrowdsourcingAnswerRepository;
import com.llm.eval.repository.CrowdsourcingTaskRepository;
import com.llm.eval.service.CrowdsourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crowdsourcing")
@Tag(name = "Crowdsourcing API", description = "众包任务管理接口")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CrowdsourcingController {

    private final CrowdsourcingService crowdsourcingService;
    private final CrowdsourcingAnswerRepository answerRepository;
    private final CrowdsourcingTaskRepository taskRepository;

    @Autowired
    public CrowdsourcingController(
            CrowdsourcingService crowdsourcingService, 
            CrowdsourcingAnswerRepository answerRepository,
            CrowdsourcingTaskRepository taskRepository) {
        this.crowdsourcingService = crowdsourcingService;
        this.answerRepository = answerRepository;
        this.taskRepository = taskRepository;
    }

    // 任务管理
    @GetMapping("/tasks")
    @Operation(summary = "获取所有众包任务")
    public ResponseEntity<List<com.llm.eval.model.CrowdsourcingTask>> getAllTasks() {
        return ResponseEntity.ok(crowdsourcingService.getAllTasks());
    }

    @GetMapping("/tasks/page")
    @Operation(summary = "分页获取众包任务")
    public ResponseEntity<?> getPagedTasks(HttpServletRequest request) {
        try {
            // 从请求中获取所有参数，并打印日志
            String pageStr = request.getParameter("page");
            String sizeStr = request.getParameter("size");
            String status = request.getParameter("status");
            String sort = request.getParameter("sort");
            String direction = request.getParameter("direction");
            
            System.out.println("原始请求参数 - page: [" + pageStr + "], size: [" + sizeStr + 
                              "], status: [" + status + "], sort: [" + sort + "], direction: [" + direction + "]");
            
            // 设置默认值
            int page = 0;
            int size = 10;
            String sortField = "taskId";
            Sort.Direction sortDirection = Sort.Direction.DESC;
            
            // 解析页码
            if (pageStr != null && !pageStr.isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr);
                    System.out.println("解析页码成功: " + page);
                } catch (NumberFormatException e) {
                    System.err.println("页码参数无效: [" + pageStr + "], 使用默认值0");
                }
            }
            
            // 解析每页大小
            if (sizeStr != null && !sizeStr.isEmpty()) {
                try {
                    size = Integer.parseInt(sizeStr);
                    System.out.println("解析每页大小成功: " + size);
                } catch (NumberFormatException e) {
                    System.err.println("每页大小参数无效: [" + sizeStr + "], 使用默认值10");
                }
            }
            
            // 解析排序字段
            if (sort != null && !sort.isEmpty()) {
                sortField = sort;
            }
            
            // 解析排序方向
            if (direction != null && direction.equalsIgnoreCase("ASC")) {
                sortDirection = Sort.Direction.ASC;
            }
            
            System.out.println("处理后的参数 - page: [" + page + "], size: [" + size + 
                              "], sortField: [" + sortField + "], sortDirection: [" + sortDirection + "]");
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(page, size, sortDirection, sortField);
            
            // 查询数据
            Page<CrowdsourcingTask> tasks;
            
            if (status != null && !status.isEmpty()) {
                // 将状态转换为大写，以匹配枚举值
                String upperCaseStatus = status.toUpperCase();
                System.out.println("尝试转换状态参数: [" + status + "] -> [" + upperCaseStatus + "]");
                
                try {
                    // 验证状态值是否有效
                    CrowdsourcingTask.TaskStatus taskStatus = CrowdsourcingTask.TaskStatus.valueOf(upperCaseStatus);
                    System.out.println("状态参数有效: [" + upperCaseStatus + "]");
                    
                    // 使用转换后的大写状态值进行查询
                    tasks = crowdsourcingService.getPagedTasks(pageable, upperCaseStatus);
                    System.out.println("按状态[" + upperCaseStatus + "]查询，找到[" + tasks.getTotalElements() + "]条记录");
                    
                    // 打印找到的任务状态
                    if (tasks.hasContent()) {
                        System.out.println("找到的任务状态列表:");
                        for (CrowdsourcingTask task : tasks.getContent()) {
                            System.out.println("任务ID: " + task.getTaskId() + ", 状态: [" + task.getStatus() + "]");
                            
                            // 为每个任务添加已提交的答案数量
                            long submittedAnswerCount = answerRepository.countByTaskId(task.getTaskId());
                            task.setCurrentAnswers((int)submittedAnswerCount);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("无效的状态值: [" + status + "], 查询所有状态");
                    tasks = crowdsourcingService.getPagedTasks(pageable, null);
                    System.out.println("查询所有状态，找到[" + tasks.getTotalElements() + "]条记录");
                    
                    // 打印找到的所有任务状态
                    if (tasks.hasContent()) {
                        System.out.println("找到的所有任务状态列表:");
                        for (CrowdsourcingTask task : tasks.getContent()) {
                            System.out.println("任务ID: " + task.getTaskId() + ", 状态: [" + task.getStatus() + "]");
                            
                            // 为每个任务添加已提交的答案数量
                            long submittedAnswerCount = answerRepository.countByTaskId(task.getTaskId());
                            task.setCurrentAnswers((int)submittedAnswerCount);
                        }
                    }
                }
            } else {
                tasks = crowdsourcingService.getPagedTasks(pageable, null);
                System.out.println("查询所有状态，找到[" + tasks.getTotalElements() + "]条记录");
                
                // 打印找到的所有任务状态
                if (tasks.hasContent()) {
                    System.out.println("找到的所有任务状态列表:");
                    for (CrowdsourcingTask task : tasks.getContent()) {
                        System.out.println("任务ID: " + task.getTaskId() + ", 状态: [" + task.getStatus() + "]");
                        
                        // 为每个任务添加已提交的答案数量
                        long submittedAnswerCount = answerRepository.countByTaskId(task.getTaskId());
                        task.setCurrentAnswers((int)submittedAnswerCount);
                    }
                }
            }
            
            // 直接返回任务分页对象，避免双重包装
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            System.err.println("获取分页任务列表发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取任务列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "获取特定众包任务")
    public ResponseEntity<?> getTaskById(@PathVariable("id") Integer id) {
        try {
            System.out.println("获取众包任务详情，任务ID: " + id);
            
            if (id == null) {
                System.err.println("任务ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "任务ID不能为空"));
            }
            
            Optional<CrowdsourcingTask> taskOptional = crowdsourcingService.getTaskById(id);
            
            if (taskOptional.isPresent()) {
                CrowdsourcingTask task = taskOptional.get();
                System.out.println("成功获取任务详情: 任务ID=" + task.getTaskId() + ", 标题=" + task.getTitle() + ", 状态=" + task.getStatus());
                
                // 获取任务的实际答案数量
                List<CrowdsourcingTaskQuestion> taskQuestions = crowdsourcingService.getTaskQuestions(id);
                int totalAnswerCount = 0;
                
                if (taskQuestions != null && !taskQuestions.isEmpty()) {
                    for (CrowdsourcingTaskQuestion question : taskQuestions) {
                        Integer answerCount = question.getCurrentAnswerCount();
                        if (answerCount != null) {
                            totalAnswerCount += answerCount;
                        }
                    }
                    System.out.println("从TaskQuestion计算得出任务答案总数: " + totalAnswerCount);
                }
                
                // 通过count查询获取任务的所有答案数量
                long submittedAnswerCount = answerRepository.countByTaskId(id);
                long approvedAnswerCount = answerRepository.countByTaskIdAndStatus(id, CrowdsourcingAnswer.AnswerStatus.APPROVED);
                
                System.out.println("任务答案数量统计 - 提交总数: " + submittedAnswerCount + ", 已批准数: " + approvedAnswerCount);
                
                // 更新任务的当前答案数量
                if (task.getCurrentAnswers() == null || task.getCurrentAnswers() != approvedAnswerCount) {
                    task.setCurrentAnswers((int)approvedAnswerCount);
                    taskRepository.save(task);
                    System.out.println("已更新任务的当前答案数量为: " + approvedAnswerCount);
                }
                
                // 更新任务的当前答案数量为已提交的答案总数
                task.setCurrentAnswers((int)submittedAnswerCount);
                taskRepository.save(task);
                
                // 创建简化的DTO
                Map<String, Object> result = new HashMap<>();
                result.put("taskId", task.getTaskId());
                result.put("title", task.getTitle());
                result.put("description", task.getDescription());
                result.put("standardQuestionId", task.getStandardQuestionId());
                result.put("requiredAnswers", task.getRequiredAnswers());
                result.put("currentAnswers", task.getCurrentAnswers());
                result.put("submittedAnswerCount", submittedAnswerCount);
                result.put("approvedAnswerCount", approvedAnswerCount);
                result.put("status", task.getStatus().toString());
                result.put("createdAt", task.getCreatedAt());
                result.put("updatedAt", task.getUpdatedAt());
                result.put("createdBy", task.getCreatedBy());
                
                return ResponseEntity.ok(result);
            } else {
                System.err.println("未找到ID为 " + id + " 的任务");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "未找到ID为 " + id + " 的任务"));
            }
        } catch (Exception e) {
            System.err.println("获取任务详情时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取任务详情失败: " + e.getMessage()));
        }
    }

    @PostMapping("/tasks")
    @Operation(summary = "创建新众包任务")
    public ResponseEntity<?> createTask(
            @Valid @RequestBody com.llm.eval.model.CrowdsourcingTask task) {
        try {
            System.out.println("创建众包任务");
            
            // 确保前端传递了标准问题ID
            if (task.getStandardQuestionId() == null) {
                System.err.println("标准问题ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "创建任务时必须指定标准问题ID"));
            }
            
            com.llm.eval.model.CrowdsourcingTask createdTask = crowdsourcingService.createTask(task);
            System.out.println("任务创建成功，任务ID: " + createdTask.getTaskId());
            
            // 创建简化的DTO
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", createdTask.getTaskId());
            result.put("title", createdTask.getTitle());
            result.put("description", createdTask.getDescription());
            result.put("standardQuestionId", createdTask.getStandardQuestionId());
            result.put("requiredAnswers", createdTask.getRequiredAnswers());
            result.put("currentAnswers", createdTask.getCurrentAnswers());
            result.put("status", createdTask.getStatus().toString());
            result.put("createdAt", createdTask.getCreatedAt());
            result.put("createdBy", createdTask.getCreatedBy());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            System.err.println("创建任务时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "创建任务失败: " + e.getMessage()));
        }
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "更新众包任务")
    public ResponseEntity<?> updateTask(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.CrowdsourcingTask task) {
        try {
            System.out.println("更新众包任务，任务ID: " + id);
            
            if (id == null) {
                System.err.println("任务ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "任务ID不能为空"));
            }
            
            com.llm.eval.model.CrowdsourcingTask updatedTask = crowdsourcingService.updateTask(id, task);
            System.out.println("任务更新成功，任务ID: " + updatedTask.getTaskId());
            
            // 创建简化的DTO
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", updatedTask.getTaskId());
            result.put("title", updatedTask.getTitle());
            result.put("description", updatedTask.getDescription());
            result.put("standardQuestionId", updatedTask.getStandardQuestionId());
            result.put("requiredAnswers", updatedTask.getRequiredAnswers());
            result.put("currentAnswers", updatedTask.getCurrentAnswers());
            result.put("status", updatedTask.getStatus().toString());
            result.put("updatedAt", updatedTask.getUpdatedAt());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("更新任务时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "更新任务失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "删除众包任务")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Integer id) {
        try {
            crowdsourcingService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tasks/{id}/publish")
    @Operation(summary = "发布众包任务")
    public ResponseEntity<?> publishTask(@PathVariable("id") Integer id) {
        try {
            System.out.println("发布众包任务，任务ID: " + id);
            
            if (id == null) {
                System.err.println("任务ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "任务ID不能为空"));
            }
            
            com.llm.eval.model.CrowdsourcingTask publishedTask = crowdsourcingService.publishTask(id);
            System.out.println("任务发布成功，任务ID: " + publishedTask.getTaskId());
            
            // 获取任务的所有答案数量
            long submittedAnswerCount = answerRepository.countByTaskId(id);
            
            // 创建简化的DTO
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", publishedTask.getTaskId());
            result.put("title", publishedTask.getTitle());
            result.put("status", publishedTask.getStatus().toString());
            result.put("updatedAt", publishedTask.getUpdatedAt());
            result.put("submittedAnswerCount", submittedAnswerCount);
            result.put("requiredAnswers", publishedTask.getRequiredAnswers());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("发布任务时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "发布任务失败: " + e.getMessage()));
        }
    }

    @PostMapping("/tasks/{id}/complete")
    @Operation(summary = "完成众包任务")
    public ResponseEntity<?> completeTask(@PathVariable("id") Integer id) {
        try {
            System.out.println("完成众包任务，任务ID: " + id);
            
            if (id == null) {
                System.err.println("任务ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "任务ID不能为空"));
            }
            
            com.llm.eval.model.CrowdsourcingTask completedTask = crowdsourcingService.completeTask(id);
            System.out.println("任务完成成功，任务ID: " + completedTask.getTaskId());
            
            // 获取任务的所有答案数量
            long submittedAnswerCount = answerRepository.countByTaskId(id);
            
            // 创建简化的DTO
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", completedTask.getTaskId());
            result.put("title", completedTask.getTitle());
            result.put("status", completedTask.getStatus().toString());
            result.put("updatedAt", completedTask.getUpdatedAt());
            result.put("submittedAnswerCount", submittedAnswerCount);
            result.put("requiredAnswers", completedTask.getRequiredAnswers());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("完成任务时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "完成任务失败: " + e.getMessage()));
        }
    }

    // 答案管理
    @GetMapping("/answers")
    @Operation(summary = "获取所有众包答案")
    public ResponseEntity<List<com.llm.eval.model.CrowdsourcingAnswer>> getAllAnswers() {
        return ResponseEntity.ok(crowdsourcingService.getAllAnswers());
    }

    @GetMapping("/answers/{id}")
    @Operation(summary = "获取特定众包答案")
    public ResponseEntity<?> getAnswerById(@PathVariable("id") Integer id) {
        try {
            Optional<CrowdsourcingAnswer> answerOptional = crowdsourcingService.getAnswerById(id);
            if (answerOptional.isPresent()) {
                return ResponseEntity.ok(answerOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到ID为 " + id + " 的答案");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取答案详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/tasks/{taskId}/answers")
    @Operation(summary = "获取特定任务的所有答案")
    public ResponseEntity<List<com.llm.eval.model.CrowdsourcingAnswer>> getAnswersByTaskId(
            @PathVariable("taskId") Integer taskId) {
        return ResponseEntity.ok(crowdsourcingService.getAnswersByTaskId(taskId));
    }

    @PostMapping("/tasks/{taskId}/answers")
    @Operation(summary = "提交众包答案")
    public ResponseEntity<?> submitAnswer(
            @PathVariable("taskId") Integer taskId,
            @Valid @RequestBody com.llm.eval.model.CrowdsourcingAnswer answer) {
        try {
            System.out.println("提交众包答案，任务ID: " + taskId);
            System.out.println("请求体: " + answer);
            
            // 验证任务ID
            if (taskId == null) {
                System.err.println("任务ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "任务ID不能为空"));
            }
            
            // 验证任务是否存在
            Optional<CrowdsourcingTask> taskOptional;
            try {
                taskOptional = crowdsourcingService.getTaskById(taskId);
            } catch (Exception e) {
                System.err.println("获取任务失败: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取任务失败: " + e.getMessage()));
            }
            
            if (!taskOptional.isPresent()) {
                System.err.println("未找到ID为 " + taskId + " 的任务");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "任务不存在"));
            }
            
            // 验证任务状态
            CrowdsourcingTask task = taskOptional.get();
            if (task.getStatus() != CrowdsourcingTask.TaskStatus.PUBLISHED) {
                System.err.println("任务状态不是PUBLISHED，当前状态: " + task.getStatus());
                return ResponseEntity.badRequest().body(Map.of("message", "任务未发布或已完成，不能提交答案"));
            }
            
            // 验证请求体
            if (answer.getStandardQuestionId() == null) {
                System.err.println("标准问题ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "标准问题ID不能为空"));
            }
            
            if (answer.getAnswerText() == null || answer.getAnswerText().trim().isEmpty()) {
                System.err.println("答案内容为空");
                return ResponseEntity.badRequest().body(Map.of("message", "答案内容不能为空"));
            }
            
            // 设置任务ID
            answer.setTaskId(taskId);
            System.out.println("处理后的答案数据: " + answer);
            
            // 提交答案
            com.llm.eval.model.CrowdsourcingAnswer submittedAnswer;
            try {
                submittedAnswer = crowdsourcingService.submitAnswer(answer);
            } catch (Exception e) {
                System.err.println("保存答案失败: " + e.getMessage());
                e.printStackTrace();
                
                // 增强异常信息记录
                Throwable rootCause = e;
                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }
                System.err.println("根本原因: " + rootCause.getMessage());
                System.err.println("异常类型: " + rootCause.getClass().getName());
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "保存答案失败: " + e.getMessage(),
                    "rootCause", rootCause.getMessage(),
                    "exceptionType", rootCause.getClass().getName()
                ));
            }
            
            System.out.println("答案提交成功，答案ID: " + submittedAnswer.getAnswerId());
            
            // 创建简化的DTO，避免序列化问题
            Map<String, Object> result = new HashMap<>();
            result.put("answerId", submittedAnswer.getAnswerId());
            result.put("taskId", submittedAnswer.getTaskId());
            result.put("standardQuestionId", submittedAnswer.getStandardQuestionId());
            result.put("answerText", submittedAnswer.getAnswerText());
            result.put("status", submittedAnswer.getStatus().toString());
            result.put("createdAt", submittedAnswer.getCreatedAt());
            
            // 只在非空时添加可选字段
            if (submittedAnswer.getContributorName() != null) {
                result.put("contributorName", submittedAnswer.getContributorName());
            }
            if (submittedAnswer.getContributorEmail() != null) {
                result.put("contributorEmail", submittedAnswer.getContributorEmail());
            }
            if (submittedAnswer.getOccupation() != null) {
                result.put("occupation", submittedAnswer.getOccupation());
            }
            if (submittedAnswer.getExpertise() != null) {
                result.put("expertise", submittedAnswer.getExpertise());
            }
            if (submittedAnswer.getReferences() != null) {
                result.put("references", submittedAnswer.getReferences());
            }
            
            // 直接返回DTO对象，避免序列化问题
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            System.err.println("提交答案时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "提交答案失败: " + e.getMessage()));
        }
    }

    @PutMapping("/answers/{id}")
    @Operation(summary = "更新众包答案")
    public ResponseEntity<?> updateAnswer(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.CrowdsourcingAnswer answer) {
        try {
            com.llm.eval.model.CrowdsourcingAnswer updatedAnswer = crowdsourcingService.updateAnswer(id, answer);
            
            // 创建简化的DTO
            Map<String, Object> result = new HashMap<>();
            result.put("answerId", updatedAnswer.getAnswerId());
            result.put("taskId", updatedAnswer.getTaskId());
            result.put("standardQuestionId", updatedAnswer.getStandardQuestionId());
            result.put("contributorName", updatedAnswer.getContributorName());
            result.put("contributorEmail", updatedAnswer.getContributorEmail());
            result.put("answerText", updatedAnswer.getAnswerText());
            result.put("status", updatedAnswer.getStatus().toString());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "更新答案失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/answers/{id}")
    @Operation(summary = "删除众包答案")
    public ResponseEntity<Void> deleteAnswer(@PathVariable("id") Integer id) {
        try {
            crowdsourcingService.deleteAnswer(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 审核相关
    @PostMapping("/answers/{id}/review")
    @Operation(summary = "审核众包答案")
    public ResponseEntity<?> reviewAnswer(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> reviewData) {
        try {
            System.out.println("审核众包答案，答案ID: " + id);
            
            if (id == null) {
                System.err.println("答案ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "答案ID不能为空"));
            }
            
            Boolean approved = (Boolean) reviewData.get("approved");
            String comment = (String) reviewData.get("comment");
            
            System.out.println("审核数据: approved=" + approved + ", comment=" + comment);
            
            com.llm.eval.model.CrowdsourcingAnswer reviewedAnswer = 
                crowdsourcingService.reviewAnswer(id, approved, comment);
            
            System.out.println("答案审核成功，答案ID: " + reviewedAnswer.getAnswerId() + 
                              ", 状态: " + reviewedAnswer.getStatus());
            
            // 创建简化的DTO
            Map<String, Object> result = new HashMap<>();
            result.put("answerId", reviewedAnswer.getAnswerId());
            result.put("taskId", reviewedAnswer.getTaskId());
            result.put("standardQuestionId", reviewedAnswer.getStandardQuestionId());
            result.put("status", reviewedAnswer.getStatus().toString());
            result.put("reviewComment", reviewedAnswer.getReviewComment());
            result.put("qualityScore", reviewedAnswer.getQualityScore());
            
            // 直接返回审核后的答案对象
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("审核答案时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "审核答案失败: " + e.getMessage()));
        }
    }

    @PostMapping("/answers/{id}/promote")
    @Operation(summary = "将众包答案提升为标准答案")
    public ResponseEntity<?> promoteToStandardAnswer(@PathVariable("id") Integer id) {
        try {
            System.out.println("将众包答案提升为标准答案，答案ID: " + id);
            
            if (id == null) {
                System.err.println("答案ID为空");
                return ResponseEntity.badRequest().body(Map.of("message", "答案ID不能为空"));
            }
            
            com.llm.eval.model.StandardAnswer standardAnswer = crowdsourcingService.promoteToStandardAnswer(id);
            System.out.println("答案提升成功，标准答案ID: " + standardAnswer.getStandardAnswerId());
            
            // 创建简化的DTO
            Map<String, Object> result = new HashMap<>();
            result.put("standardAnswerId", standardAnswer.getStandardAnswerId());
            result.put("answer", standardAnswer.getAnswer());
            result.put("sourceType", standardAnswer.getSourceType().toString());
            result.put("sourceId", standardAnswer.getSourceId());
            result.put("selectionReason", standardAnswer.getSelectionReason());
            result.put("isFinal", standardAnswer.getIsFinal());
            
            // 直接返回创建的标准答案对象
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("提升答案时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "提升答案失败: " + e.getMessage()));
        }
    }

    @GetMapping("/tasks/{taskId}/questions")
    @Operation(summary = "获取任务关联的问题")
    public ResponseEntity<?> getTaskQuestions(
            @PathVariable("taskId") Integer taskId) {
        try {
            System.out.println("获取任务关联的问题，任务ID: " + taskId);
            
            if (taskId == null) {
                System.err.println("任务ID为空");
                return ResponseEntity.badRequest().body("任务ID不能为空");
            }
            
            List<com.llm.eval.model.CrowdsourcingTaskQuestion> questions = crowdsourcingService.getTaskQuestions(taskId);
            System.out.println("成功获取任务关联问题，数量: " + questions.size());
            
            // 创建简单的DTO列表，避免序列化代理对象的问题
            List<Map<String, Object>> simplifiedQuestions = questions.stream().map(q -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", q.getId());
                map.put("taskId", q.getTaskId());
                map.put("standardQuestionId", q.getStandardQuestionId());
                map.put("currentAnswerCount", q.getCurrentAnswerCount());
                map.put("isCompleted", q.getIsCompleted());
                map.put("createdAt", q.getCreatedAt());
                // 不包含task和standardQuestion关联对象，避免延迟加载问题
                return map;
            }).collect(Collectors.toList());
            
            // 返回简化的DTO对象
            return ResponseEntity.ok(simplifiedQuestions);
        } catch (Exception e) {
            System.err.println("获取任务关联问题时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取任务关联问题失败: " + e.getMessage());
        }
    }
} 