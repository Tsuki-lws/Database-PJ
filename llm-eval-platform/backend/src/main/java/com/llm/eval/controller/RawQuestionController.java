package com.llm.eval.controller;

import com.github.pagehelper.PageInfo;
import com.llm.eval.dto.QueryParams;
import com.llm.eval.dto.RawQuestionConverter;
import com.llm.eval.model.RawAnswer;
import com.llm.eval.model.RawQuestion;
import com.llm.eval.service.RawAnswerService;
import com.llm.eval.service.RawQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/raw-questions")
@Tag(name = "Raw Question API", description = "原始问题管理接口")
public class RawQuestionController {

    private final RawQuestionService rawQuestionService;
    private final RawAnswerService rawAnswerService;

    @Autowired
    public RawQuestionController(RawQuestionService rawQuestionService, RawAnswerService rawAnswerService) {
        this.rawQuestionService = rawQuestionService;
        this.rawAnswerService = rawAnswerService;
    }

    @GetMapping
    @Operation(summary = "获取原始问题列表")
    public ResponseEntity<PageInfo<RawQuestion>> listRawQuestions(QueryParams queryParams) {
        return ResponseEntity.ok(rawQuestionService.findRawQuestionsByPage(queryParams));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取原始问题")
    public ResponseEntity<RawQuestion> getRawQuestion(@PathVariable Integer id) {
        return rawQuestionService.findRawQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "创建原始问题")
    public ResponseEntity<RawQuestion> createRawQuestion(@RequestBody RawQuestion rawQuestion) {
        return ResponseEntity.ok(rawQuestionService.saveRawQuestion(rawQuestion));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新原始问题")
    public ResponseEntity<RawQuestion> updateRawQuestion(@PathVariable Integer id, @RequestBody RawQuestion rawQuestion) {
        rawQuestion.setQuestionId(id);
        return ResponseEntity.ok(rawQuestionService.updateRawQuestion(rawQuestion));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除原始问题")
    public ResponseEntity<Void> deleteRawQuestion(@PathVariable Integer id) {
        rawQuestionService.deleteRawQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{questionId}/answers")
    @Operation(summary = "获取原始问题的回答列表")
    public ResponseEntity<List<RawAnswer>> getRawAnswers(@PathVariable Integer questionId) {
        return ResponseEntity.ok(rawAnswerService.findAnswersByQuestionId(questionId));
    }

    @PostMapping("/{questionId}/answers")
    @Operation(summary = "为原始问题添加回答")
    public ResponseEntity<RawAnswer> addRawAnswer(@PathVariable Integer questionId, @RequestBody RawAnswer rawAnswer) {
        // 查询问题对象
        RawQuestion question = rawQuestionService.findRawQuestionById(questionId)
                .orElseThrow(() -> new RuntimeException("原始问题不存在: " + questionId));
        
        // 设置问题关联
        rawAnswer.setQuestion(question);
        
        return ResponseEntity.ok(rawAnswerService.saveRawAnswer(rawAnswer));
    }

    @PostMapping("/{id}/convert")
    @Operation(summary = "将原始问题转换为标准问题")
    public ResponseEntity<Map<String, Object>> convertToStandardQuestion(@PathVariable Integer id, @RequestBody RawQuestionConverter converter) {
        return ResponseEntity.ok(rawQuestionService.convertToStandardQuestion(id, converter));
    }

    @GetMapping("/sources")
    @Operation(summary = "获取所有问题来源")
    public ResponseEntity<List<String>> getAllSources() {
        return ResponseEntity.ok(rawQuestionService.getAllSources());
    }

    @GetMapping("/stats/by-source")
    @Operation(summary = "获取各来源的问题数量")
    public ResponseEntity<Map<String, Long>> getQuestionCountsBySource() {
        return ResponseEntity.ok(rawQuestionService.getQuestionCountsBySource());
    }
} 