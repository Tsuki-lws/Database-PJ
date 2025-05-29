package com.llm.eval.controller;

import com.llm.eval.model.Tag;
import com.llm.eval.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag API", description = "标签管理接口")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(summary = "获取所有标签")
    public ResponseEntity<List<com.llm.eval.model.Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取标签")
    public ResponseEntity<com.llm.eval.model.Tag> getTagById(@PathVariable("id") Integer id) {
        return tagService.getTagById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "创建新标签")
    public ResponseEntity<com.llm.eval.model.Tag> createTag(@Valid @RequestBody com.llm.eval.model.Tag tag) {
        com.llm.eval.model.Tag createdTag = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标签")
    public ResponseEntity<com.llm.eval.model.Tag> updateTag(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.Tag tag) {
        try {
            com.llm.eval.model.Tag updatedTag = tagService.updateTag(id, tag);
            return ResponseEntity.ok(updatedTag);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签")
    public ResponseEntity<Void> deleteTag(@PathVariable("id") Integer id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/question/{questionId}")
    @Operation(summary = "获取问题的标签")
    public ResponseEntity<List<com.llm.eval.model.Tag>> getTagsByQuestionId(@PathVariable("questionId") Integer questionId) {
        return ResponseEntity.ok(tagService.getTagsByQuestionId(questionId));
    }
    
    @PostMapping("/question/{questionId}")
    @Operation(summary = "给问题添加标签")
    public ResponseEntity<Void> addTagToQuestion(
            @PathVariable("questionId") Integer questionId,
            @RequestBody List<Integer> tagIds) {
        try {
            tagService.addTagsToQuestion(questionId, tagIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/question/{questionId}/{tagId}")
    @Operation(summary = "从问题中移除标签")
    public ResponseEntity<Void> removeTagFromQuestion(
            @PathVariable("questionId") Integer questionId,
            @PathVariable("tagId") Integer tagId) {
        try {
            tagService.removeTagFromQuestion(questionId, tagId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 