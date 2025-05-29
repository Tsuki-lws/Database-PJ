package com.llm.eval.controller;

import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.service.DatasetVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/datasets")
@Tag(name = "Dataset Version API", description = "数据集版本管理接口")
public class DatasetVersionController {

    private final DatasetVersionService datasetVersionService;

    @Autowired
    public DatasetVersionController(DatasetVersionService datasetVersionService) {
        this.datasetVersionService = datasetVersionService;
    }

    @GetMapping
    @Operation(summary = "获取所有数据集版本")
    public ResponseEntity<List<com.llm.eval.model.DatasetVersion>> getAllDatasetVersions() {
        return ResponseEntity.ok(datasetVersionService.getAllDatasetVersions());
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
    public ResponseEntity<Set<com.llm.eval.model.StandardQuestion>> getQuestionsInDatasetVersion(@PathVariable("id") Integer id) {
        try {
            Set<com.llm.eval.model.StandardQuestion> questions = datasetVersionService.getQuestionsInDatasetVersion(id);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "创建新数据集版本")
    public ResponseEntity<com.llm.eval.model.DatasetVersion> createDatasetVersion(@Valid @RequestBody com.llm.eval.model.DatasetVersion datasetVersion) {
        com.llm.eval.model.DatasetVersion createdVersion = datasetVersionService.createDatasetVersion(datasetVersion);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
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
    public ResponseEntity<com.llm.eval.model.DatasetVersion> publishDatasetVersion(@PathVariable("id") Integer id) {
        try {
            com.llm.eval.model.DatasetVersion publishedVersion = datasetVersionService.publishDatasetVersion(id);
            return ResponseEntity.ok(publishedVersion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
} 