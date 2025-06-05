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
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    public ResponseEntity<List<Map<String, Object>>> getAllCategories() {
        // 使用带有问题数量的分类列表
        List<QuestionCategory> categories = categoryService.getCategoriesWithQuestionCount();
        
        // 将实体转换为简单的Map对象，避免循环引用问题
        List<Map<String, Object>> result = categories.stream().map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", category.getCategoryId());
            map.put("name", category.getName());
            map.put("description", category.getDescription());
            map.put("questionCount", category.getQuestionCount()); // 添加问题数量
            
            // 如果有父分类，只包含父分类的ID和名称
            if (category.getParent() != null) {
                Map<String, Object> parentMap = new HashMap<>();
                parentMap.put("categoryId", category.getParent().getCategoryId());
                parentMap.put("name", category.getParent().getName());
                map.put("parent", parentMap);
            } else {
                map.put("parent", null);
            }
            
            // 如果有子分类，只包含子分类的ID和名称
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                List<Map<String, Object>> childrenList = category.getChildren().stream()
                    .map(child -> {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("categoryId", child.getCategoryId());
                        childMap.put("name", child.getName());
                        return childMap;
                    })
                    .collect(Collectors.toList());
                map.put("children", childrenList);
            } else {
                map.put("children", new ArrayList<>());
            }
            
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取分类")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable("id") Integer id) {
        return categoryService.getCategoryById(id)
                .map(category -> {
                    // 将实体转换为简单的Map对象，避免循环引用问题
                    Map<String, Object> map = new HashMap<>();
                    map.put("categoryId", category.getCategoryId());
                    map.put("name", category.getName());
                    map.put("description", category.getDescription());
                    
                    // 如果有父分类，只包含父分类的ID和名称
                    if (category.getParent() != null) {
                        Map<String, Object> parentMap = new HashMap<>();
                        parentMap.put("categoryId", category.getParent().getCategoryId());
                        parentMap.put("name", category.getParent().getName());
                        map.put("parent", parentMap);
                    } else {
                        map.put("parent", null);
                    }
                    
                    // 如果有子分类，只包含子分类的ID和名称
                    if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                        List<Map<String, Object>> childrenList = category.getChildren().stream()
                            .map(child -> {
                                Map<String, Object> childMap = new HashMap<>();
                                childMap.put("categoryId", child.getCategoryId());
                                childMap.put("name", child.getName());
                                return childMap;
                            })
                            .collect(Collectors.toList());
                        map.put("children", childrenList);
                    } else {
                        map.put("children", new ArrayList<>());
                    }
                    
                    // 不包含问题列表，避免循环引用
                    
                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/root")
    @Operation(summary = "获取根分类")
    public ResponseEntity<List<Map<String, Object>>> getRootCategories() {
        List<QuestionCategory> categories = categoryService.getRootCategories();
        
        // 将实体转换为简单的Map对象，避免循环引用问题
        List<Map<String, Object>> result = categories.stream().map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", category.getCategoryId());
            map.put("name", category.getName());
            map.put("description", category.getDescription());
            
            // 如果有子分类，只包含子分类的ID和名称
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                List<Map<String, Object>> childrenList = category.getChildren().stream()
                    .map(child -> {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("categoryId", child.getCategoryId());
                        childMap.put("name", child.getName());
                        return childMap;
                    })
                    .collect(Collectors.toList());
                map.put("children", childrenList);
            } else {
                map.put("children", new ArrayList<>());
            }
            
            // 不包含问题列表，避免循环引用
            
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "获取子分类")
    public ResponseEntity<List<Map<String, Object>>> getChildCategories(@PathVariable("id") Integer id) {
        List<QuestionCategory> categories = categoryService.getChildCategories(id);
        
        // 将实体转换为简单的Map对象，避免循环引用问题
        List<Map<String, Object>> result = categories.stream().map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", category.getCategoryId());
            map.put("name", category.getName());
            map.put("description", category.getDescription());
            
            // 父分类信息
            Map<String, Object> parentMap = new HashMap<>();
            parentMap.put("categoryId", id);
            map.put("parent", parentMap);
            
            // 如果有子分类，只包含子分类的ID和名称
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                List<Map<String, Object>> childrenList = category.getChildren().stream()
                    .map(child -> {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("categoryId", child.getCategoryId());
                        childMap.put("name", child.getName());
                        return childMap;
                    })
                    .collect(Collectors.toList());
                map.put("children", childrenList);
            } else {
                map.put("children", new ArrayList<>());
            }
            
            // 不包含问题列表，避免循环引用
            
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "创建新分类")
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody QuestionCategory category) {
        QuestionCategory createdCategory = categoryService.createCategory(category);
        
        // 将实体转换为简单的Map对象，避免循环引用问题
        Map<String, Object> result = new HashMap<>();
        result.put("categoryId", createdCategory.getCategoryId());
        result.put("name", createdCategory.getName());
        result.put("description", createdCategory.getDescription());
        
        // 如果有父分类，只包含父分类的ID和名称
        if (createdCategory.getParent() != null) {
            Map<String, Object> parentMap = new HashMap<>();
            parentMap.put("categoryId", createdCategory.getParent().getCategoryId());
            parentMap.put("name", createdCategory.getParent().getName());
            result.put("parent", parentMap);
        } else {
            result.put("parent", null);
        }
        
        // 子分类应该是空的，因为是新创建的分类
        result.put("children", new ArrayList<>());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable("id") Integer id,
            @Valid @RequestBody QuestionCategory category) {
        try {
            QuestionCategory updatedCategory = categoryService.updateCategory(id, category);
            
            // 将实体转换为简单的Map对象，避免循环引用问题
            Map<String, Object> result = new HashMap<>();
            result.put("categoryId", updatedCategory.getCategoryId());
            result.put("name", updatedCategory.getName());
            result.put("description", updatedCategory.getDescription());
            
            // 如果有父分类，只包含父分类的ID和名称
            if (updatedCategory.getParent() != null) {
                Map<String, Object> parentMap = new HashMap<>();
                parentMap.put("categoryId", updatedCategory.getParent().getCategoryId());
                parentMap.put("name", updatedCategory.getParent().getName());
                result.put("parent", parentMap);
            } else {
                result.put("parent", null);
            }
            
            // 如果有子分类，只包含子分类的ID和名称
            if (updatedCategory.getChildren() != null && !updatedCategory.getChildren().isEmpty()) {
                List<Map<String, Object>> childrenList = updatedCategory.getChildren().stream()
                    .map(child -> {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("categoryId", child.getCategoryId());
                        childMap.put("name", child.getName());
                        return childMap;
                    })
                    .collect(Collectors.toList());
                result.put("children", childrenList);
            } else {
                result.put("children", new ArrayList<>());
            }
            
            return ResponseEntity.ok(result);
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
    public ResponseEntity<List<Map<String, Object>>> getCategoriesWithQuestionCount() {
        List<QuestionCategory> categories = categoryService.getCategoriesWithQuestionCount();
        
        // 将实体转换为简单的Map对象，避免循环引用问题
        List<Map<String, Object>> result = categories.stream().map(category -> {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", category.getCategoryId());
            map.put("name", category.getName());
            map.put("description", category.getDescription());
            map.put("questionCount", category.getQuestionCount());
            
            // 如果有父分类，只包含父分类的ID和名称
            if (category.getParent() != null) {
                Map<String, Object> parentMap = new HashMap<>();
                parentMap.put("categoryId", category.getParent().getCategoryId());
                parentMap.put("name", category.getParent().getName());
                map.put("parent", parentMap);
            } else {
                map.put("parent", null);
            }
            
            // 如果有子分类，只包含子分类的ID和名称
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                List<Map<String, Object>> childrenList = category.getChildren().stream()
                    .map(child -> {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("categoryId", child.getCategoryId());
                        childMap.put("name", child.getName());
                        return childMap;
                    })
                    .collect(Collectors.toList());
                map.put("children", childrenList);
            } else {
                map.put("children", new ArrayList<>());
            }
            
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
} 