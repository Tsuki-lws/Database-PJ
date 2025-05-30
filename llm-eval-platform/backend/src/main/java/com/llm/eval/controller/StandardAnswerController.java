package com.llm.eval.controller;

import com.llm.eval.model.AnswerKeyPoint;
import com.llm.eval.model.StandardAnswer;
import com.llm.eval.service.StandardAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/answers")
@Tag(name = "Standard Answer API", description = "标准答案管理接口")
public class StandardAnswerController {

    private final StandardAnswerService answerService;

    @Autowired
    public StandardAnswerController(StandardAnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping
    @Operation(summary = "获取所有标准答案")
    public ResponseEntity<List<com.llm.eval.model.StandardAnswer>> getAllAnswers() {
        return ResponseEntity.ok(answerService.getAllAnswers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> getAnswerById(@PathVariable("id") Integer id) {
        return answerService.getAnswerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "获取问题的所有标准答案")
    public ResponseEntity<List<com.llm.eval.model.StandardAnswer>> getAnswersByQuestionId(
            @PathVariable("questionId") Integer questionId) {
        return ResponseEntity.ok(answerService.getAnswersByQuestionId(questionId));
    }

    @GetMapping("/question/{questionId}/final")
    @Operation(summary = "获取问题的最终标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> getFinalAnswerByQuestionId(
            @PathVariable("questionId") Integer questionId) {
        return answerService.getFinalAnswerByQuestionId(questionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "创建新标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> createAnswer(
            @Valid @RequestBody com.llm.eval.model.StandardAnswer answer) {
        com.llm.eval.model.StandardAnswer createdAnswer = answerService.createAnswer(answer);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnswer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> updateAnswer(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.StandardAnswer answer) {
        try {
            com.llm.eval.model.StandardAnswer updatedAnswer = answerService.updateAnswer(id, answer);
            return ResponseEntity.ok(updatedAnswer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标准答案")
    public ResponseEntity<Void> deleteAnswer(@PathVariable("id") Integer id) {
        try {
            answerService.deleteAnswer(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/set-final")
    @Operation(summary = "设置为最终标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> setAsFinalAnswer(@PathVariable("id") Integer id) {
        try {
            com.llm.eval.model.StandardAnswer finalAnswer = answerService.setAsFinalAnswer(id);
            return ResponseEntity.ok(finalAnswer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 关键点管理
    @PostMapping("/{id}/key-points")
    @Operation(summary = "添加关键点")
    public ResponseEntity<com.llm.eval.model.AnswerKeyPoint> addKeyPoint(
            @PathVariable("id") Integer answerId,
            @Valid @RequestBody com.llm.eval.model.AnswerKeyPoint keyPoint) {
        try {
            com.llm.eval.model.AnswerKeyPoint createdKeyPoint = answerService.addKeyPoint(answerId, keyPoint);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdKeyPoint);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/key-points")
    @Operation(summary = "获取答案的所有关键点")
    public ResponseEntity<List<com.llm.eval.model.AnswerKeyPoint>> getKeyPointsByAnswerId(
            @PathVariable("id") Integer answerId) {
        return ResponseEntity.ok(answerService.getKeyPointsByAnswerId(answerId));
    }
    
    @PutMapping("/{id}/key-points/{keyPointId}")
    @Operation(summary = "更新关键点")
    public ResponseEntity<com.llm.eval.model.AnswerKeyPoint> updateKeyPoint(
            @PathVariable("id") Integer answerId,
            @PathVariable("keyPointId") Integer keyPointId,
            @Valid @RequestBody com.llm.eval.model.AnswerKeyPoint keyPoint) {
        try {
            com.llm.eval.model.AnswerKeyPoint updatedKeyPoint = answerService.updateKeyPoint(answerId, keyPointId, keyPoint);
            return ResponseEntity.ok(updatedKeyPoint);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}/key-points/{keyPointId}")
    @Operation(summary = "删除关键点")
    public ResponseEntity<Void> deleteKeyPoint(
            @PathVariable("id") Integer answerId,
            @PathVariable("keyPointId") Integer keyPointId) {
        try {
            answerService.deleteKeyPoint(answerId, keyPointId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 