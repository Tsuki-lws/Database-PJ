package com.llm.eval.controller;

import com.llm.eval.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistics API", description = "统计数据接口")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
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