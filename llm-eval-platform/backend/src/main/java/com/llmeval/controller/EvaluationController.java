package com.llmeval.controller;

import com.llmeval.dto.EvaluationDetailDTO;
import com.llmeval.dto.ManualEvaluationDTO;
import com.llmeval.dto.UnevaluatedAnswerDTO;
import com.llmeval.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 获取待评测的答案列表
     */
    @GetMapping("/unevaluated")
    public ResponseEntity<Page<UnevaluatedAnswerDTO>> getUnevaluatedAnswers(
            @RequestParam(required = false) Integer modelId,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Integer categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(evaluationService.getUnevaluatedAnswers(modelId, questionType, categoryId, pageable));
    }

    /**
     * 获取评测详情（用于人工评测）
     */
    @GetMapping("/detail/{answerId}")
    public ResponseEntity<EvaluationDetailDTO> getEvaluationDetail(@PathVariable Integer answerId) {
        return ResponseEntity.ok(evaluationService.getEvaluationDetail(answerId));
    }

    /**
     * 提交人工评测结果
     */
    @PostMapping("/manual")
    public ResponseEntity<Void> submitManualEvaluation(@RequestBody ManualEvaluationDTO evaluationDTO) {
        evaluationService.submitManualEvaluation(evaluationDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 导入评测结果
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importEvaluations(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = evaluationService.importEvaluations(file);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取模型对比数据
     */
    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getModelComparisonData(@RequestParam String modelIds) {
        List<Integer> ids = List.of(modelIds.split(",")).stream()
                .map(Integer::parseInt)
                .toList();
        return ResponseEntity.ok(evaluationService.getModelComparisonData(ids));
    }
} 