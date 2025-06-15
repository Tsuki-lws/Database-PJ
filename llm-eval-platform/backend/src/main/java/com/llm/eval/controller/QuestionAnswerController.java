package com.llm.eval.controller;

import com.llm.eval.dto.LlmAnswerDTO;
import com.llm.eval.dto.EvaluationDTO;
import com.llm.eval.service.LlmAnswerService;
import com.llm.eval.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Question Answer API", description = "问题回答管理接口")
public class QuestionAnswerController {

    private final LlmAnswerService answerService;
    private final EvaluationService evaluationService;

    @Autowired
    public QuestionAnswerController(LlmAnswerService answerService, EvaluationService evaluationService) {
        this.answerService = answerService;
        this.evaluationService = evaluationService;
    }

    @GetMapping("/answers")
    @Operation(summary = "获取问题回答列表")
    public ResponseEntity<?> getQuestionAnswers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "questionId") Integer questionId,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "evaluationType", required = false) String evaluationType) {
        
        try {
            // 获取问题回答列表
            List<LlmAnswerDTO> answers = answerService.getAnswersByQuestionId(questionId);
            
            // 应用筛选条件
            List<LlmAnswerDTO> filteredAnswers = answers;
            if (modelId != null) {
                filteredAnswers = filteredAnswers.stream()
                    .filter(answer -> answer.getModel() != null && 
                            answer.getModel().getModelId().equals(modelId))
                    .toList();
            }
            
            // 如果需要按评测类型筛选，需要先获取评测信息
            if (evaluationType != null) {
                filteredAnswers = filteredAnswers.stream()
                    .filter(answer -> {
                        List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByAnswerId(answer.getLlmAnswerId());
                        return evaluations.stream()
                            .anyMatch(eval -> eval.getMethod() != null && eval.getMethod().equals(evaluationType));
                    })
                    .toList();
            }
            
            // 手动分页
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, filteredAnswers.size());
            
            List<LlmAnswerDTO> pagedAnswers = fromIndex < filteredAnswers.size() ? 
                    filteredAnswers.subList(fromIndex, toIndex) : 
                    List.of();
            
            // 为每个回答添加评测信息
            List<Map<String, Object>> resultList = pagedAnswers.stream()
                .map(answer -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("answerId", answer.getLlmAnswerId());
                    result.put("modelName", answer.getModel() != null ? 
                            answer.getModel().getName() + " " + answer.getModel().getVersion() : "未知模型");
                    result.put("content", answer.getContent());
                    
                    // 获取评测信息
                    List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByAnswerId(answer.getLlmAnswerId());
                    if (!evaluations.isEmpty()) {
                        EvaluationDTO latestEval = evaluations.get(0); // 假设第一个是最新的
                        result.put("score", latestEval.getScore());
                        result.put("evaluationType", latestEval.getMethod());
                        result.put("evaluatedAt", latestEval.getCreatedAt());
                    } else {
                        result.put("score", null);
                        result.put("evaluationType", null);
                        result.put("evaluatedAt", null);
                    }
                    
                    return result;
                })
                .toList();
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", resultList);
            response.put("totalElements", filteredAnswers.size());
            response.put("totalPages", (int) Math.ceil((double) filteredAnswers.size() / size));
            response.put("size", size);
            response.put("number", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取问题回答列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/answers/{answerId}")
    @Operation(summary = "获取回答详情")
    public ResponseEntity<?> getAnswerDetail(@PathVariable("answerId") Integer answerId) {
        try {
            LlmAnswerDTO answer = answerService.getAnswerById(answerId)
                .orElseThrow(() -> new RuntimeException("回答不存在"));
            
            // 获取评测信息
            List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByAnswerId(answerId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("answer", answer);
            result.put("evaluations", evaluations);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取回答详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 