package com.llm.eval.controller;

import com.llm.eval.model.Tag;
import com.llm.eval.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

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
            // 添加日志记录
            System.out.println("尝试从问题 " + questionId + " 中移除标签 " + tagId);
            
            tagService.removeTagFromQuestion(questionId, tagId);
            
            // 成功日志
            System.out.println("成功从问题 " + questionId + " 中移除标签 " + tagId);
            
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            // 当问题或标签不存在时
            System.err.println("移除标签失败(404): " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // 当标签不在问题中或问题没有标签时
            System.err.println("移除标签失败(400): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            // 其他未预期的异常
            System.err.println("移除标签失败(500): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Internal server error: " + e.getMessage())
                    .build();
        }
    }
    
    @GetMapping("/with-count")
    @Operation(summary = "获取带问题数量的标签列表")
    public ResponseEntity<Map<String, Long>> getTagsWithQuestionCount() {
        return ResponseEntity.ok(tagService.getTagsWithQuestionCount());
    }
    
    @GetMapping("/with-count/details")
    @Operation(summary = "获取带问题数量和详细信息的标签列表")
    public ResponseEntity<Map<String, Object>> getTagsWithQuestionCountDetails() {
        return ResponseEntity.ok(tagService.getTagsWithQuestionCountDetails());
    }
    
    @GetMapping("/search")
    @Operation(summary = "根据标签查询问题")
    public ResponseEntity<List<Integer>> getQuestionsByTags(@RequestParam List<Integer> tagIds) {
        return ResponseEntity.ok(tagService.getQuestionsByTags(tagIds));
    }
} 