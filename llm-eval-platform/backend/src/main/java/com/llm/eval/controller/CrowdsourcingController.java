package com.llm.eval.controller;

import com.llm.eval.model.CrowdsourcingAnswer;
import com.llm.eval.model.CrowdsourcingTask;
import com.llm.eval.service.CrowdsourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crowdsourcing")
@Tag(name = "Crowdsourcing API", description = "众包任务管理接口")
public class CrowdsourcingController {

    private final CrowdsourcingService crowdsourcingService;

    @Autowired
    public CrowdsourcingController(CrowdsourcingService crowdsourcingService) {
        this.crowdsourcingService = crowdsourcingService;
    }

    // 任务管理
    @GetMapping("/tasks")
    @Operation(summary = "获取所有众包任务")
    public ResponseEntity<List<com.llm.eval.model.CrowdsourcingTask>> getAllTasks() {
        return ResponseEntity.ok(crowdsourcingService.getAllTasks());
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "获取特定众包任务")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingTask> getTaskById(@PathVariable("id") Integer id) {
        return crowdsourcingService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tasks")
    @Operation(summary = "创建新众包任务")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingTask> createTask(
            @Valid @RequestBody com.llm.eval.model.CrowdsourcingTask task) {
        com.llm.eval.model.CrowdsourcingTask createdTask = crowdsourcingService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "更新众包任务")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingTask> updateTask(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.CrowdsourcingTask task) {
        try {
            com.llm.eval.model.CrowdsourcingTask updatedTask = crowdsourcingService.updateTask(id, task);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "删除众包任务")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Integer id) {
        try {
            crowdsourcingService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tasks/{id}/publish")
    @Operation(summary = "发布众包任务")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingTask> publishTask(@PathVariable("id") Integer id) {
        try {
            com.llm.eval.model.CrowdsourcingTask publishedTask = crowdsourcingService.publishTask(id);
            return ResponseEntity.ok(publishedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/tasks/{id}/complete")
    @Operation(summary = "完成众包任务")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingTask> completeTask(@PathVariable("id") Integer id) {
        try {
            com.llm.eval.model.CrowdsourcingTask completedTask = crowdsourcingService.completeTask(id);
            return ResponseEntity.ok(completedTask);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 答案管理
    @GetMapping("/answers")
    @Operation(summary = "获取所有众包答案")
    public ResponseEntity<List<com.llm.eval.model.CrowdsourcingAnswer>> getAllAnswers() {
        return ResponseEntity.ok(crowdsourcingService.getAllAnswers());
    }

    @GetMapping("/answers/{id}")
    @Operation(summary = "获取特定众包答案")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingAnswer> getAnswerById(@PathVariable("id") Integer id) {
        return crowdsourcingService.getAnswerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tasks/{taskId}/answers")
    @Operation(summary = "获取特定任务的所有答案")
    public ResponseEntity<List<com.llm.eval.model.CrowdsourcingAnswer>> getAnswersByTaskId(
            @PathVariable("taskId") Integer taskId) {
        return ResponseEntity.ok(crowdsourcingService.getAnswersByTaskId(taskId));
    }

    @PostMapping("/tasks/{taskId}/answers")
    @Operation(summary = "提交众包答案")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingAnswer> submitAnswer(
            @PathVariable("taskId") Integer taskId,
            @Valid @RequestBody com.llm.eval.model.CrowdsourcingAnswer answer) {
        try {
            answer.setTaskId(taskId);
            com.llm.eval.model.CrowdsourcingAnswer submittedAnswer = crowdsourcingService.submitAnswer(answer);
            return ResponseEntity.status(HttpStatus.CREATED).body(submittedAnswer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/answers/{id}")
    @Operation(summary = "更新众包答案")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingAnswer> updateAnswer(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.CrowdsourcingAnswer answer) {
        try {
            com.llm.eval.model.CrowdsourcingAnswer updatedAnswer = crowdsourcingService.updateAnswer(id, answer);
            return ResponseEntity.ok(updatedAnswer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/answers/{id}")
    @Operation(summary = "删除众包答案")
    public ResponseEntity<Void> deleteAnswer(@PathVariable("id") Integer id) {
        try {
            crowdsourcingService.deleteAnswer(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 审核相关
    @PostMapping("/answers/{id}/review")
    @Operation(summary = "审核众包答案")
    public ResponseEntity<com.llm.eval.model.CrowdsourcingAnswer> reviewAnswer(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> reviewData) {
        try {
            Boolean approved = (Boolean) reviewData.get("approved");
            String comment = (String) reviewData.get("comment");
            
            com.llm.eval.model.CrowdsourcingAnswer reviewedAnswer = 
                crowdsourcingService.reviewAnswer(id, approved, comment);
            return ResponseEntity.ok(reviewedAnswer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/answers/{id}/promote")
    @Operation(summary = "将众包答案提升为标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> promoteToStandardAnswer(@PathVariable("id") Integer id) {
        try {
            com.llm.eval.model.StandardAnswer standardAnswer = crowdsourcingService.promoteToStandardAnswer(id);
            return ResponseEntity.ok(standardAnswer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
} 