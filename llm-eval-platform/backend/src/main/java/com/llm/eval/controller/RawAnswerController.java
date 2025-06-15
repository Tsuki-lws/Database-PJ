package com.llm.eval.controller;

import com.llm.eval.model.RawAnswer;
import com.llm.eval.service.RawAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/raw-answers")
@Tag(name = "Raw Answer API", description = "原始回答管理接口")
public class RawAnswerController {

    private final RawAnswerService rawAnswerService;

    @Autowired
    public RawAnswerController(RawAnswerService rawAnswerService) {
        this.rawAnswerService = rawAnswerService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取原始回答")
    public ResponseEntity<RawAnswer> getRawAnswer(@PathVariable Integer id) {
        return rawAnswerService.findRawAnswerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新原始回答")
    public ResponseEntity<RawAnswer> updateRawAnswer(@PathVariable Integer id, @RequestBody RawAnswer rawAnswer) {
        // 先查询现有回答
        RawAnswer existingAnswer = rawAnswerService.findRawAnswerById(id)
                .orElseThrow(() -> new RuntimeException("原始回答不存在: " + id));
        
        // 设置ID和问题关联
        rawAnswer.setAnswerId(id);
        rawAnswer.setQuestion(existingAnswer.getQuestion());
        
        return ResponseEntity.ok(rawAnswerService.updateRawAnswer(rawAnswer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除原始回答")
    public ResponseEntity<Void> deleteRawAnswer(@PathVariable Integer id) {
        rawAnswerService.deleteRawAnswer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @Operation(summary = "获取原始回答总数")
    public ResponseEntity<Map<String, Long>> countRawAnswers() {
        Map<String, Long> result = new HashMap<>();
        result.put("count", rawAnswerService.countRawAnswers());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count/by-question/{questionId}")
    @Operation(summary = "获取指定问题的回答数量")
    public ResponseEntity<Map<String, Long>> countByQuestionId(@PathVariable Integer questionId) {
        Map<String, Long> result = new HashMap<>();
        result.put("count", rawAnswerService.countByQuestionId(questionId));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/convert")
    @Operation(summary = "将原始回答转换为标准答案")
    public ResponseEntity<Map<String, Object>> convertToStandardAnswer(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        try {
            // 获取参数
            String answer = (String) params.get("answer");
            Integer standardQuestionId = null;
            if (params.get("standardQuestionId") instanceof Number) {
                standardQuestionId = ((Number) params.get("standardQuestionId")).intValue();
            }
            Boolean isFinal = (Boolean) params.get("isFinal");
            
            if (answer == null || standardQuestionId == null) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "参数不完整，需要answer和standardQuestionId");
                return ResponseEntity.badRequest().body(errorResult);
            }
            
            // 查找原始回答
            RawAnswer rawAnswer = rawAnswerService.findRawAnswerById(id)
                    .orElseThrow(() -> new RuntimeException("原始回答不存在: " + id));
            
            // 调用服务执行转换
            Map<String, Object> result = rawAnswerService.convertToStandardAnswer(id, standardQuestionId, answer, isFinal);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "转换失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }
} 