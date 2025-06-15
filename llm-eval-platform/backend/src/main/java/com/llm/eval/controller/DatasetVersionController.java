package com.llm.eval.controller;

import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.service.DatasetVersionService;
import com.llm.eval.dto.DatasetVersionDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/datasets")
@Tag(name = "Dataset Version API", description = "数据集版本管理接口")
public class DatasetVersionController {

    private final DatasetVersionService datasetVersionService;
    private final com.llm.eval.service.LlmAnswerService llmAnswerService;
    private final com.llm.eval.service.EvaluationService evaluationService;

    @Autowired
    public DatasetVersionController(DatasetVersionService datasetVersionService,
                                   com.llm.eval.service.LlmAnswerService llmAnswerService,
                                   com.llm.eval.service.EvaluationService evaluationService) {
        this.datasetVersionService = datasetVersionService;
        this.llmAnswerService = llmAnswerService;
        this.evaluationService = evaluationService;
    }

    @GetMapping
    @Operation(summary = "获取所有数据集版本")
    public ResponseEntity<List<DatasetVersionDTO>> getAllDatasetVersions() {
        List<DatasetVersion> versions = datasetVersionService.getAllDatasetVersions();
        List<DatasetVersionDTO> versionDTOs = versions.stream()
            .map(version -> DatasetVersionDTO.fromEntity(version, false))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(versionDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取数据集版本")
    public ResponseEntity<com.llm.eval.model.DatasetVersion> getDatasetVersionById(@PathVariable("id") Integer id) {
        return datasetVersionService.getDatasetVersionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/published")
    @Operation(summary = "获取已发布的数据集版本")
    public ResponseEntity<List<com.llm.eval.model.DatasetVersion>> getPublishedDatasetVersions() {
        return ResponseEntity.ok(datasetVersionService.getPublishedDatasetVersions());
    }

    @GetMapping("/{id}/questions")
    @Operation(summary = "获取数据集版本中的问题")
    public ResponseEntity<List<Map<String, Object>>> getQuestionsInDatasetVersion(@PathVariable("id") Integer id) {
        try {
            Set<com.llm.eval.model.StandardQuestion> questions = datasetVersionService.getQuestionsInDatasetVersion(id);
            
            // 将问题转换为Map并添加hasStandardAnswer字段
            List<Map<String, Object>> result = new ArrayList<>();
            for (com.llm.eval.model.StandardQuestion question : questions) {
                Map<String, Object> questionMap = new HashMap<>();
                questionMap.put("standardQuestionId", question.getStandardQuestionId());
                questionMap.put("question", question.getQuestion());
                questionMap.put("questionType", question.getQuestionType());
                questionMap.put("difficulty", question.getDifficulty());
                questionMap.put("status", question.getStatus());
                
                // 检查是否有标准答案
                boolean hasStandardAnswer = question.getStandardAnswers() != null && !question.getStandardAnswers().isEmpty();
                questionMap.put("hasStandardAnswer", hasStandardAnswer);
                
                // 添加分类信息（如果存在）
                if (question.getCategory() != null) {
                    Map<String, Object> categoryMap = new HashMap<>();
                    categoryMap.put("categoryId", question.getCategory().getCategoryId());
                    categoryMap.put("categoryName", question.getCategory().getName());
                    questionMap.put("category", categoryMap);
                }
                
                // 获取该问题的评测数量和平均分
                try {
                    // 获取问题的所有答案
                    List<com.llm.eval.dto.LlmAnswerDTO> answers = llmAnswerService.getAnswersByQuestionId(question.getStandardQuestionId());
                    
                    // 统计评测数量
                    int evaluationCount = 0;
                    BigDecimal totalScore = BigDecimal.ZERO;
                    int scoredAnswers = 0;
                    
                    for (com.llm.eval.dto.LlmAnswerDTO answer : answers) {
                        // 获取答案的评测结果
                        List<com.llm.eval.dto.EvaluationDTO> evaluations = evaluationService.getEvaluationsByAnswerId(answer.getLlmAnswerId());
                        if (evaluations != null && !evaluations.isEmpty()) {
                            evaluationCount += evaluations.size();
                            
                            // 计算平均分
                            for (com.llm.eval.dto.EvaluationDTO eval : evaluations) {
                                if (eval.getScore() != null) {
                                    totalScore = totalScore.add(eval.getScore());
                                    scoredAnswers++;
                                }
                            }
                        }
                    }
                    
                    questionMap.put("answerCount", answers.size());
                    questionMap.put("evaluationCount", evaluationCount);
                    
                    // 计算平均分
                    if (scoredAnswers > 0) {
                        BigDecimal avgScore = totalScore.divide(BigDecimal.valueOf(scoredAnswers), 2, RoundingMode.HALF_UP);
                        questionMap.put("avgScore", avgScore);
                    } else {
                        questionMap.put("avgScore", 0);
                    }
                } catch (Exception e) {
                    System.err.println("获取问题评测数据失败: " + e.getMessage());
                    questionMap.put("answerCount", 0);
                    questionMap.put("evaluationCount", 0);
                    questionMap.put("avgScore", 0);
                }
                
                result.add(questionMap);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取数据集问题失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/evaluation-stats")
    @Operation(summary = "获取数据集评测统计信息")
    public ResponseEntity<Map<String, Object>> getDatasetEvaluationStats(@PathVariable("id") Integer id) {
        try {
            // 获取数据集中的所有问题
            Set<com.llm.eval.model.StandardQuestion> questions = datasetVersionService.getQuestionsInDatasetVersion(id);
            
            // 统计数据
            int totalQuestions = questions.size();
            int evaluatedQuestions = 0;
            BigDecimal totalScore = BigDecimal.ZERO;
            int scoredQuestions = 0;
            
            // 遍历所有问题，统计已评测的问题数量和总分
            for (com.llm.eval.model.StandardQuestion question : questions) {
                try {
                    // 获取问题的所有答案
                    List<com.llm.eval.dto.LlmAnswerDTO> answers = llmAnswerService.getAnswersByQuestionId(question.getStandardQuestionId());
                    
                    boolean hasEvaluation = false;
                    for (com.llm.eval.dto.LlmAnswerDTO answer : answers) {
                        // 获取答案的评测结果
                        List<com.llm.eval.dto.EvaluationDTO> evaluations = evaluationService.getEvaluationsByAnswerId(answer.getLlmAnswerId());
                        if (evaluations != null && !evaluations.isEmpty()) {
                            hasEvaluation = true;
                            
                            // 计算总分
                            for (com.llm.eval.dto.EvaluationDTO eval : evaluations) {
                                if (eval.getScore() != null) {
                                    totalScore = totalScore.add(eval.getScore());
                                    scoredQuestions++;
                                }
                            }
                        }
                    }
                    
                    // 如果问题有评测，计数加1
                    if (hasEvaluation) {
                        evaluatedQuestions++;
                    }
                } catch (Exception e) {
                    System.err.println("获取问题评测数据失败: " + e.getMessage());
                }
            }
            
            // 计算平均分
            BigDecimal avgScore = BigDecimal.ZERO;
            if (scoredQuestions > 0) {
                avgScore = totalScore.divide(BigDecimal.valueOf(scoredQuestions), 2, RoundingMode.HALF_UP);
            }
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("totalQuestions", totalQuestions);
            result.put("evaluatedCount", evaluatedQuestions);
            result.put("avgScore", avgScore);
            result.put("isFullyEvaluated", totalQuestions > 0 && evaluatedQuestions >= totalQuestions);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取数据集评测统计信息失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "获取数据集评测统计信息失败: " + e.getMessage())
            );
        }
    }

    @PostMapping
    @Operation(summary = "创建新数据集版本")
    public ResponseEntity<com.llm.eval.model.DatasetVersion> createDatasetVersion(@Valid @RequestBody com.llm.eval.model.DatasetVersion datasetVersion) {
        try {
            System.out.println("接收到创建数据集版本请求: " + datasetVersion.getName());
            
            // 检查是否包含questionIds参数（前端传递）
            if (datasetVersion.getQuestionIds() != null && !datasetVersion.getQuestionIds().isEmpty()) {
                System.out.println("包含问题ID: " + datasetVersion.getQuestionIds().size() + "个");
                // 使用增强版本的创建方法，支持直接关联问题
                return ResponseEntity.status(HttpStatus.CREATED).body(
                    datasetVersionService.createDatasetVersionWithQuestions(datasetVersion, datasetVersion.getQuestionIds())
                );
            } else {
                System.out.println("不包含问题ID，创建空数据集版本");
                com.llm.eval.model.DatasetVersion createdVersion = datasetVersionService.createDatasetVersion(datasetVersion);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
            }
        } catch (Exception e) {
            System.err.println("创建数据集版本失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新数据集版本")
    public ResponseEntity<com.llm.eval.model.DatasetVersion> updateDatasetVersion(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.DatasetVersion datasetVersion) {
        try {
            com.llm.eval.model.DatasetVersion updatedVersion = datasetVersionService.updateDatasetVersion(id, datasetVersion);
            return ResponseEntity.ok(updatedVersion);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据集版本")
    public ResponseEntity<Void> deleteDatasetVersion(@PathVariable("id") Integer id) {
        try {
            datasetVersionService.deleteDatasetVersion(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/questions")
    @Operation(summary = "添加问题到数据集版本")
    public ResponseEntity<com.llm.eval.model.DatasetVersion> addQuestionsToDatasetVersion(
            @PathVariable("id") Integer id,
            @RequestBody Set<Integer> questionIds) {
        try {
            com.llm.eval.model.DatasetVersion updatedVersion = datasetVersionService.addQuestionsToDatasetVersion(id, questionIds);
            return ResponseEntity.ok(updatedVersion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}/questions")
    @Operation(summary = "从数据集版本中移除问题")
    public ResponseEntity<com.llm.eval.model.DatasetVersion> removeQuestionsFromDatasetVersion(
            @PathVariable("id") Integer id,
            @RequestBody Set<Integer> questionIds) {
        try {
            com.llm.eval.model.DatasetVersion updatedVersion = datasetVersionService.removeQuestionsFromDatasetVersion(id, questionIds);
            return ResponseEntity.ok(updatedVersion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布数据集版本")
    public ResponseEntity<?> publishDatasetVersion(@PathVariable("id") Integer id) {
        try {
            com.llm.eval.model.DatasetVersion publishedVersion = datasetVersionService.publishDatasetVersion(id);
            return ResponseEntity.ok(publishedVersion);
        } catch (IllegalStateException e) {
            // 处理没有问题的情况
            System.err.println("发布数据集版本失败: " + e.getMessage());
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "发布失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            System.err.println("发布数据集版本失败: " + e.getMessage());
            e.printStackTrace();
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "发布失败");
            errorResponse.put("message", "服务器内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 新增版本管理端点
    @GetMapping("/paged")
    @Operation(summary = "分页获取数据集版本列表")
    public ResponseEntity<com.llm.eval.dto.PagedResponseDTO<com.llm.eval.dto.DatasetVersionDTO>> getVersionsPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            System.out.println("分页获取数据集版本列表: page=" + page + ", size=" + size + 
                              ", isPublished=" + isPublished + ", keyword=" + keyword +
                              ", sortBy=" + sortBy + ", sortDir=" + sortDir);
            
            // 创建分页请求，注意前端页码从1开始，后端从0开始
            int adjustedPage = Math.max(0, page - 1); // 确保页码不小于0
            
            // 创建排序对象
            org.springframework.data.domain.Sort sort;
            if ("asc".equalsIgnoreCase(sortDir)) {
                sort = org.springframework.data.domain.Sort.by(sortBy).ascending();
            } else {
                sort = org.springframework.data.domain.Sort.by(sortBy).descending();
            }
            
            org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(adjustedPage, size, sort);
            
            System.out.println("调整后的分页参数: page=" + adjustedPage + ", size=" + size + 
                              ", sort=" + sort);

            com.llm.eval.dto.PagedResponseDTO<com.llm.eval.dto.DatasetVersionDTO> result = null;
            
            try {
                // 根据参数决定调用哪个查询方法
                if (keyword != null && !keyword.trim().isEmpty()) {
                    if (isPublished != null) {
                        // 按名称搜索 + 发布状态筛选
                        result = datasetVersionService.searchVersionsByNameAndPublishStatus(
                            keyword.trim(), isPublished, pageable);
                    } else {
                        // 仅按名称搜索
                        result = datasetVersionService.searchVersionsByName(keyword.trim(), pageable);
                    }
                } else if (isPublished != null) {
                    // 仅按发布状态筛选
                    result = datasetVersionService.getVersionsByPublishStatus(isPublished, pageable);
                } else {
                    // 无筛选条件
                    result = datasetVersionService.getVersionsPaged(pageable);
                }
            } catch (Exception e) {
                System.err.println("查询数据集版本失败: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
            // 确保返回的当前页码与请求的页码一致（前端从1开始）
            if (result != null) {
                // 强制设置当前页码为请求的页码
                result.setCurrentPage(page);
                System.out.println("强制设置当前页码为请求页码: " + page);
                
                // 如果内容为空但总数不为0，可能是页码超出范围，尝试获取第一页
                if (result.getContent() != null && result.getContent().isEmpty() && result.getTotal() > 0) {
                    System.out.println("检测到页码可能超出范围，尝试获取第一页");
                    pageable = org.springframework.data.domain.PageRequest.of(0, size, sort);
                    
                    // 重新查询第一页数据
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        if (isPublished != null) {
                            result = datasetVersionService.searchVersionsByNameAndPublishStatus(
                                keyword.trim(), isPublished, pageable);
                        } else {
                            result = datasetVersionService.searchVersionsByName(keyword.trim(), pageable);
                        }
                    } else if (isPublished != null) {
                        result = datasetVersionService.getVersionsByPublishStatus(isPublished, pageable);
                    } else {
                        result = datasetVersionService.getVersionsPaged(pageable);
                    }
                    
                    // 设置为第一页
                    result.setCurrentPage(1);
                }
            }
            
            System.out.println("查询结果: 总数=" + (result != null ? result.getTotal() : "null") + 
                              ", 数据条数=" + (result != null && result.getContent() != null ? result.getContent().size() : "null") +
                              ", 当前页码=" + (result != null ? result.getCurrentPage() : "null"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取数据集版本列表失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "取消发布数据集版本")
    public ResponseEntity<com.llm.eval.model.DatasetVersion> unpublishDatasetVersion(@PathVariable("id") Integer id) {
        try {
            com.llm.eval.model.DatasetVersion unpublishedVersion = datasetVersionService.unpublishDatasetVersion(id);
            return ResponseEntity.ok(unpublishedVersion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/latest-published")
    @Operation(summary = "获取最新发布的版本")
    public ResponseEntity<com.llm.eval.dto.DatasetVersionDTO> getLatestPublishedVersion() {
        try {
            com.llm.eval.dto.DatasetVersionDTO versionDTO = datasetVersionService.getLatestPublishedVersion();
            return ResponseEntity.ok(versionDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/check-name")
    @Operation(summary = "检查版本名称是否已存在")
    public ResponseEntity<java.util.Map<String, Object>> checkVersionName(@RequestParam String name) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("exists", datasetVersionService.isVersionNameExists(name));
        return ResponseEntity.ok(response);
    }
}