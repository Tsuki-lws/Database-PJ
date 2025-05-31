package com.llm.eval.controller;

import com.llm.eval.dto.PagedResponseDTO;
import com.llm.eval.dto.StandardQuestionWithoutAnswerDTO;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.service.StandardQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Standard Question API", description = "标准问题管理接口")
public class StandardQuestionController {

    private final StandardQuestionService questionService;

    @Autowired
    public StandardQuestionController(StandardQuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    @Operation(summary = "获取所有标准问题")
    public ResponseEntity<List<com.llm.eval.model.StandardQuestion>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllStandardQuestions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取标准问题")
    public ResponseEntity<com.llm.eval.model.StandardQuestion> getQuestionById(@PathVariable("id") Integer id) {
        return questionService.getStandardQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "获取特定分类下的问题")
    public ResponseEntity<List<com.llm.eval.model.StandardQuestion>> getQuestionsByCategory(
            @PathVariable("categoryId") Integer categoryId) {
        return ResponseEntity.ok(questionService.getStandardQuestionsByCategoryId(categoryId));
    }

    @GetMapping("/tag/{tagId}")
    @Operation(summary = "获取特定标签下的问题")
    public ResponseEntity<List<com.llm.eval.model.StandardQuestion>> getQuestionsByTag(
            @PathVariable("tagId") Integer tagId) {
        return ResponseEntity.ok(questionService.getStandardQuestionsByTagId(tagId));
    }    @GetMapping("/without-answer")
    @Operation(summary = "获取无标准答案的问题")
    public ResponseEntity<List<StandardQuestion>> getQuestionsWithoutAnswer() {
        return ResponseEntity.ok(questionService.getQuestionsWithoutStandardAnswers());
    }
    
    @GetMapping("/without-answer/paged")
    @Operation(summary = "分页获取无标准答案的问题")
    public ResponseEntity<PagedResponseDTO<StandardQuestionWithoutAnswerDTO>> getQuestionsWithoutAnswerPaged(
            @Parameter(description = "页码（从1开始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "问题类型") @RequestParam(required = false) StandardQuestion.QuestionType questionType,
            @Parameter(description = "难度级别") @RequestParam(required = false) StandardQuestion.DifficultyLevel difficulty,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        // 创建排序对象
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        // 创建分页对象 (Spring分页从0开始)
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        // 获取分页数据
        Page<StandardQuestion> questionPage = questionService.getQuestionsWithoutStandardAnswersWithFilters(
                categoryId, questionType, difficulty, pageable);
        
        // 转换为DTO
        List<StandardQuestionWithoutAnswerDTO> dtoList = questionPage.getContent().stream()
                .map(StandardQuestionWithoutAnswerDTO::fromEntity)
                .toList();
        
        // 构建响应
        PagedResponseDTO<StandardQuestionWithoutAnswerDTO> response = 
                PagedResponseDTO.fromPageWithMapper(questionPage, dtoList);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "创建新标准问题")
    public ResponseEntity<com.llm.eval.model.StandardQuestion> createQuestion(
            @Valid @RequestBody com.llm.eval.model.StandardQuestion question) {
        com.llm.eval.model.StandardQuestion createdQuestion = questionService.createStandardQuestion(question);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标准问题")
    public ResponseEntity<com.llm.eval.model.StandardQuestion> updateQuestion(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.StandardQuestion question) {
        try {
            com.llm.eval.model.StandardQuestion updatedQuestion = questionService.updateStandardQuestion(id, question);
            return ResponseEntity.ok(updatedQuestion);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标准问题")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("id") Integer id) {
        try {
            questionService.deleteStandardQuestion(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/tags/{tagId}")
    @Operation(summary = "为问题添加标签")
    public ResponseEntity<Void> addTagToQuestion(
            @PathVariable("id") Integer questionId,
            @PathVariable("tagId") Integer tagId) {
        try {
            Set<Integer> tagIds = new HashSet<>(Collections.singletonList(tagId));
            questionService.addTagsToQuestion(questionId, tagIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}/tags/{tagId}")
    @Operation(summary = "移除问题的标签")
    public ResponseEntity<Void> removeTagFromQuestion(
            @PathVariable("id") Integer questionId,
            @PathVariable("tagId") Integer tagId) {
        try {
            Set<Integer> tagIds = new HashSet<>(Collections.singletonList(tagId));
            questionService.removeTagsFromQuestion(questionId, tagIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}