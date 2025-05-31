package com.llm.eval.controller;

import com.llm.eval.dto.PagedResponseDTO;
import com.llm.eval.dto.StandardQuestionWithoutAnswerDTO;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.service.StandardQuestionService;
import com.llm.eval.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistics API", description = "统计数据接口")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    private final StandardQuestionService standardQuestionService;
    
    @Autowired
    public StatisticsController(StatisticsService statisticsService, 
                              StandardQuestionService standardQuestionService) {
        this.statisticsService = statisticsService;
        this.standardQuestionService = standardQuestionService;
    }
    
    @GetMapping("/raw-counts")
    @Operation(summary = "获取原始问题和答案数量")
    public ResponseEntity<Map<String, Long>> getRawQuestionAndAnswerCounts() {
        return ResponseEntity.ok(statisticsService.getRawQuestionAndAnswerCounts());
    }
    
    @GetMapping("/standard-counts")
    @Operation(summary = "获取标准问题和答案数量")
    public ResponseEntity<Map<String, Long>> getStandardQuestionAndAnswerCounts() {
        return ResponseEntity.ok(statisticsService.getStandardQuestionAndAnswerCounts());
    }
    
    @GetMapping("/category-counts")
    @Operation(summary = "获取各分类的问题数量")
    public ResponseEntity<Map<String, Long>> getQuestionCountsByCategory() {
        return ResponseEntity.ok(statisticsService.getQuestionCountsByCategory());
    }
    
    @GetMapping("/tag-counts")
    @Operation(summary = "获取各标签的问题数量")
    public ResponseEntity<Map<String, Long>> getQuestionCountsByTag() {
        return ResponseEntity.ok(statisticsService.getQuestionCountsByTag());
    }
      @GetMapping("/without-standard-answer")
    @Operation(summary = "获取无标准答案的问题数量")
    public ResponseEntity<Map<String, Long>> getQuestionsWithoutStandardAnswerCount() {
        Map<String, Long> result = new HashMap<>();
        result.put("count", statisticsService.getQuestionsWithoutStandardAnswerCount());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/without-standard-answer/details")
    @Operation(summary = "获取无标准答案的问题详细列表")
    public ResponseEntity<Map<String, Object>> getQuestionsWithoutStandardAnswerDetails(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "问题类型") @RequestParam(required = false) StandardQuestion.QuestionType questionType,
            @Parameter(description = "难度级别") @RequestParam(required = false) StandardQuestion.DifficultyLevel difficulty,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        // 创建排序对象
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        // 创建分页对象 (Spring分页从0开始)
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        // 获取分页数据
        Page<StandardQuestion> questionPage = standardQuestionService.getQuestionsWithoutStandardAnswersWithFilters(
                categoryId, questionType, difficulty, pageable);
        
        // 转换为DTO
        List<StandardQuestionWithoutAnswerDTO> dtoList = questionPage.getContent().stream()
                .map(StandardQuestionWithoutAnswerDTO::fromEntity)
                .toList();
        
        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("count", questionPage.getTotalElements());
        result.put("total", questionPage.getTotalElements());
        result.put("pages", questionPage.getTotalPages());
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("questions", dtoList);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/dataset-versions")
    @Operation(summary = "获取数据集版本数量")
    public ResponseEntity<Map<String, Long>> getDatasetVersionCount() {
        Map<String, Long> result = new HashMap<>();
        result.put("count", statisticsService.getDatasetVersionCount());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/summary")
    @Operation(summary = "获取统计数据摘要")
    public ResponseEntity<Map<String, Object>> getStatisticsSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Add raw counts
        summary.put("rawCounts", statisticsService.getRawQuestionAndAnswerCounts());
        
        // Add standard counts
        summary.put("standardCounts", statisticsService.getStandardQuestionAndAnswerCounts());
        
        // Add questions without standard answer
        summary.put("questionsWithoutStandardAnswer", statisticsService.getQuestionsWithoutStandardAnswerCount());
        
        // Add dataset version count
        summary.put("datasetVersionCount", statisticsService.getDatasetVersionCount());
        
        return ResponseEntity.ok(summary);
    }
} 