package com.llm.eval.controller;

import com.llm.eval.dto.EvaluationDTO;
import com.llm.eval.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Evaluation API", description = "模型评测结果管理接口")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @Autowired
    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    @Operation(summary = "获取所有评测结果")
    public ResponseEntity<List<EvaluationDTO>> getAllEvaluations() {
        return ResponseEntity.ok(evaluationService.getAllEvaluations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取评测结果")
    public ResponseEntity<EvaluationDTO> getEvaluationById(@PathVariable("id") Integer id) {
        return evaluationService.getEvaluationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/answer/{answerId}")
    @Operation(summary = "获取特定回答的评测结果")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByAnswerId(@PathVariable("answerId") Integer answerId) {
        List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByAnswerId(answerId);
        return ResponseEntity.ok(evaluations);
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "获取特定批次的评测结果")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByBatchId(@PathVariable("batchId") Integer batchId) {
        List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByBatchId(batchId);
        return ResponseEntity.ok(evaluations);
    }

    @PostMapping
    @Operation(summary = "创建评测结果")
    public ResponseEntity<EvaluationDTO> createEvaluation(@Valid @RequestBody EvaluationDTO evaluationDTO) {
        EvaluationDTO createdEvaluation = evaluationService.createEvaluation(evaluationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvaluation);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新评测结果")
    public ResponseEntity<EvaluationDTO> updateEvaluation(
            @PathVariable("id") Integer id,
            @Valid @RequestBody EvaluationDTO evaluationDTO) {
        return evaluationService.updateEvaluation(id, evaluationDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评测结果")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable("id") Integer id) {
        return evaluationService.deleteEvaluation(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/batch/{batchId}/average")
    @Operation(summary = "获取批次的平均评分")
    public ResponseEntity<BigDecimal> getAverageScoreByBatch(@PathVariable("batchId") Integer batchId) {
        BigDecimal averageScore = evaluationService.calculateAverageScoreByBatch(batchId);
        return ResponseEntity.ok(averageScore);
    }

    @GetMapping("/batch/{batchId}/passing")
    @Operation(summary = "获取通过评测的回答数量")
    public ResponseEntity<Long> getPassingEvaluationsCount(
            @PathVariable("batchId") Integer batchId,
            @RequestParam(value = "threshold", defaultValue = "0.6") BigDecimal threshold) {
        Long passingCount = evaluationService.countPassingEvaluationsByBatch(batchId, threshold);
        return ResponseEntity.ok(passingCount);
    }

    @PostMapping("/batch/{batchId}/run")
    @Operation(summary = "执行批次评测")
    public ResponseEntity<List<EvaluationDTO>> runBatchEvaluation(@PathVariable("batchId") Integer batchId) {
        List<EvaluationDTO> evaluations = evaluationService.evaluateBatch(batchId);
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluations);
    }
} 