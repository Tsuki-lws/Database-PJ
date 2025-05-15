package com.llmeval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.llmeval.entity.StandardQuestion;
import com.llmeval.service.StandardQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions/standard")
@CrossOrigin
public class StandardQuestionController {

    @Autowired
    private StandardQuestionService standardQuestionService;

    @GetMapping("/page")
    public Page<StandardQuestion> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Long categoryId) {
        
        Page<StandardQuestion> page = new Page<>(current, size);
        LambdaQueryWrapper<StandardQuestion> wrapper = new LambdaQueryWrapper<>();
        
        if (questionType != null) {
            wrapper.eq(StandardQuestion::getQuestionType, questionType);
        }
        if (categoryId != null) {
            wrapper.eq(StandardQuestion::getCategoryId, categoryId);
        }
        
        wrapper.eq(StandardQuestion::getIsLatest, true)
               .orderByDesc(StandardQuestion::getCreatedAt);
        
        return standardQuestionService.page(page, wrapper);
    }

    @GetMapping("/{id}")
    public StandardQuestion getById(@PathVariable Long id) {
        return standardQuestionService.getById(id);
    }

    @PostMapping
    public StandardQuestion create(@RequestBody StandardQuestion question) {
        question.setVersion(1);
        question.setIsLatest(true);
        standardQuestionService.save(question);
        return question;
    }

    @PutMapping("/{id}")
    public StandardQuestion update(
            @PathVariable Long id,
            @RequestBody StandardQuestion question) {
        return standardQuestionService.createNewVersion(id, question);
    }

    @GetMapping("/latest/{originalId}")
    public StandardQuestion getLatestVersion(@PathVariable Long originalId) {
        return standardQuestionService.getLatestVersion(originalId);
    }
} 