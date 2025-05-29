package com.llm.eval.controller;

import com.llm.eval.dto.LlmModelDTO;
import com.llm.eval.service.LlmModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/models")
@Tag(name = "LLM Model API", description = "管理大语言模型接口")
public class LlmModelController {

    private final LlmModelService llmModelService;

    @Autowired
    public LlmModelController(LlmModelService llmModelService) {
        this.llmModelService = llmModelService;
    }

    @GetMapping
    @Operation(summary = "获取所有模型列表")
    public ResponseEntity<List<LlmModelDTO>> getAllModels() {
        return ResponseEntity.ok(llmModelService.getAllModels());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取模型")
    public ResponseEntity<LlmModelDTO> getModelById(@PathVariable("id") Integer id) {
        return llmModelService.getModelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "创建新模型")
    public ResponseEntity<LlmModelDTO> createModel(@Valid @RequestBody LlmModelDTO modelDTO) {
        if (llmModelService.existsByNameAndVersion(modelDTO.getName(), modelDTO.getVersion())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        LlmModelDTO createdModel = llmModelService.createModel(modelDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模型")
    public ResponseEntity<LlmModelDTO> updateModel(
            @PathVariable("id") Integer id,
            @Valid @RequestBody LlmModelDTO modelDTO) {
        return llmModelService.updateModel(id, modelDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型")
    public ResponseEntity<Void> deleteModel(@PathVariable("id") Integer id) {
        return llmModelService.deleteModel(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
} 