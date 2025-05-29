/*
package com.llm.eval.controller;

import com.llm.eval.dto.EvaluationBatchDTO;
import com.llm.eval.model.EvaluationBatch.EvaluationStatus;
import com.llm.eval.service.EvaluationBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/batches")
@Tag(name = "Evaluation Batch API", description = "评测批次管理接口")
public class EvaluationBatchController {

    private final EvaluationBatchService batchService;

    @Autowired
    public EvaluationBatchController(EvaluationBatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    @Operation(summary = "获取所有评测批次")
    public ResponseEntity<List<EvaluationBatchDTO>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取评测批次")
    public ResponseEntity<EvaluationBatchDTO> getBatchById(@PathVariable("id") Integer id) {
        return batchService.getBatchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/model/{modelId}")
    @Operation(summary = "获取特定模型的评测批次")
    public ResponseEntity<List<EvaluationBatchDTO>> getBatchesByModelId(@PathVariable("modelId") Integer modelId) {
        return ResponseEntity.ok(batchService.getBatchesByModelId(modelId));
    }

    @GetMapping("/dataset/{versionId}")
    @Operation(summary = "获取特定数据集版本的评测批次")
    public ResponseEntity<List<EvaluationBatchDTO>> getBatchesByDatasetVersionId(
            @PathVariable("versionId") Integer versionId) {
        return ResponseEntity.ok(batchService.getBatchesByDatasetVersionId(versionId));
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "获取特定状态的评测批次")
    public ResponseEntity<List<EvaluationBatchDTO>> getBatchesByStatus(
            @PathVariable("status") EvaluationStatus status) {
        return ResponseEntity.ok(batchService.getBatchesByStatus(status));
    }

    @PostMapping
    @Operation(summary = "创建新评测批次")
    public ResponseEntity<EvaluationBatchDTO> createBatch(@Valid @RequestBody EvaluationBatchDTO batchDTO) {
        EvaluationBatchDTO createdBatch = batchService.createBatch(batchDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBatch);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新评测批次")
    public ResponseEntity<EvaluationBatchDTO> updateBatch(
            @PathVariable("id") Integer id,
            @Valid @RequestBody EvaluationBatchDTO batchDTO) {
        return batchService.updateBatch(id, batchDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评测批次")
    public ResponseEntity<Void> deleteBatch(@PathVariable("id") Integer id) {
        return batchService.deleteBatch(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始执行评测批次")
    public ResponseEntity<EvaluationBatchDTO> startBatch(@PathVariable("id") Integer id) {
        if (batchService.startBatch(id)) {
            return batchService.getBatchById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "手动完成评测批次")
    public ResponseEntity<EvaluationBatchDTO> completeBatch(@PathVariable("id") Integer id) {
        if (batchService.completeBatch(id)) {
            return batchService.updateBatchMetrics(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.badRequest().build();
    }
} 
*/ 