package com.llm.eval.controller;

import com.llm.eval.model.QuestionCategory;
import com.llm.eval.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category API", description = "问题分类管理接口")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "获取所有分类")
    public ResponseEntity<List<QuestionCategory>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取分类")
    public ResponseEntity<QuestionCategory> getCategoryById(@PathVariable("id") Integer id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/root")
    @Operation(summary = "获取根分类")
    public ResponseEntity<List<QuestionCategory>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "获取子分类")
    public ResponseEntity<List<QuestionCategory>> getChildCategories(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(categoryService.getChildCategories(id));
    }

    @PostMapping
    @Operation(summary = "创建新分类")
    public ResponseEntity<QuestionCategory> createCategory(@Valid @RequestBody QuestionCategory category) {
        QuestionCategory createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public ResponseEntity<QuestionCategory> updateCategory(
            @PathVariable("id") Integer id,
            @Valid @RequestBody QuestionCategory category) {
        try {
            QuestionCategory updatedCategory = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(updatedCategory);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Integer id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/with-count")
    @Operation(summary = "获取带问题数量的分类列表")
    public ResponseEntity<List<QuestionCategory>> getCategoriesWithQuestionCount() {
        return ResponseEntity.ok(categoryService.getCategoriesWithQuestionCount());
    }
} 