package com.llm.eval.controller;

import com.llm.eval.dto.LlmAnswerDTO;
import com.llm.eval.service.LlmAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/llm-answers")
@Tag(name = "LLM Answer API", description = "大语言模型回答管理接口")
public class LlmAnswerController {

    private final LlmAnswerService llmAnswerService;

    @Autowired
    public LlmAnswerController(LlmAnswerService llmAnswerService) {
        this.llmAnswerService = llmAnswerService;
    }

    @GetMapping
    @Operation(summary = "分页获取模型回答列表")
    public ResponseEntity<Map<String, Object>> getAnswers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "questionId", required = false) Integer questionId,
            @RequestParam(value = "batchId", required = false) Integer batchId) {
        
        try {
            // 获取数据
            List<LlmAnswerDTO> answers;
            long total = 0;
            
            if (modelId != null) {
                answers = llmAnswerService.getAnswersByModelId(modelId);
                total = answers.size();
            } else if (questionId != null) {
                answers = llmAnswerService.getAnswersByQuestionId(questionId);
                total = answers.size();
            } else if (batchId != null) {
                answers = llmAnswerService.getAnswersByBatchId(batchId);
                total = answers.size();
            } else {
                answers = llmAnswerService.getAllAnswers();
                total = answers.size();
            }
            
            // 手动分页
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, answers.size());
            
            List<LlmAnswerDTO> pagedAnswers = fromIndex < answers.size() ? 
                    answers.subList(fromIndex, toIndex) : 
                    List.of();
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", pagedAnswers);
            response.put("totalElements", total);
            response.put("totalPages", (int) Math.ceil((double) total / size));
            response.put("size", size);
            response.put("number", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取模型回答列表失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/datasets")
    @Operation(summary = "获取所有包含模型回答的数据集列表")
    public ResponseEntity<?> getDatasetVersionsWithAnswers() {
        try {
            List<Map<String, Object>> datasets = llmAnswerService.getDatasetVersionsWithAnswers();
            return ResponseEntity.ok(datasets);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取数据集列表失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    @GetMapping("/datasets/{datasetId}")
    @Operation(summary = "获取特定数据集中的问题和模型回答情况")
    public ResponseEntity<?> getQuestionsWithAnswersInDataset(
            @PathVariable("datasetId") Integer datasetId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "hasAnswer", required = false) Boolean hasAnswer) {
        try {
            Map<String, Object> result = llmAnswerService.getQuestionsWithAnswersInDataset(
                    datasetId, page, size, keyword, hasAnswer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取数据集问题和模型回答失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    @GetMapping("/datasets/{datasetId}/questions/{questionId}")
    @Operation(summary = "获取特定数据集中特定问题的所有模型回答")
    public ResponseEntity<?> getModelAnswersForQuestion(
            @PathVariable("datasetId") Integer datasetId,
            @PathVariable("questionId") Integer questionId) {
        try {
            List<LlmAnswerDTO> answers = llmAnswerService.getModelAnswersForQuestionInDataset(datasetId, questionId);
            return ResponseEntity.ok(answers);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取问题的模型回答失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取模型回答详情")
    public ResponseEntity<?> getAnswerById(@PathVariable("id") Integer id) {
        try {
            Optional<LlmAnswerDTO> answer = llmAnswerService.getAnswerById(id);
            return answer.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取模型回答详情失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型回答")
    public ResponseEntity<?> deleteAnswer(@PathVariable("id") Integer id) {
        try {
            boolean deleted = llmAnswerService.deleteAnswer(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "删除模型回答失败: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }
} 