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
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/answers")
@Tag(name = "Standard Answer API", description = "标准答案管理接口")
public class StandardAnswerController {

    private final StandardAnswerService answerService;
    private static final Logger log = LoggerFactory.getLogger(StandardAnswerController.class);

    @Autowired
    public StandardAnswerController(StandardAnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping
    @Operation(summary = "获取所有标准答案")
    public ResponseEntity<Map<String, Object>> getAllAnswers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "questionId", required = false) Integer questionId,
            @RequestParam(value = "isFinal", required = false) Boolean isFinal) {
        try {
            // 获取符合条件的答案列表和总数
            List<StandardAnswer> answers = answerService.getStandardAnswers(page, size, questionId, isFinal);
            long total = answerService.countStandardAnswers(questionId, isFinal);
            
            // 将实体对象转换为Map，避免循环引用问题
            List<Map<String, Object>> result = answers.stream().map(answer -> {
                Map<String, Object> answerMap = new HashMap<>();
                answerMap.put("standardAnswerId", answer.getStandardAnswerId());
                answerMap.put("standardQuestionId", answer.getStandardQuestionId());
                answerMap.put("answer", answer.getAnswer());
                answerMap.put("sourceType", answer.getSourceType());
                answerMap.put("sourceId", answer.getSourceId());
                answerMap.put("selectionReason", answer.getSelectionReason());
                answerMap.put("selectedBy", answer.getSelectedBy());
                answerMap.put("isFinal", answer.getIsFinal());
                answerMap.put("createdAt", answer.getCreatedAt());
                answerMap.put("updatedAt", answer.getUpdatedAt());
                answerMap.put("version", answer.getVersion());
                return answerMap;
            }).collect(Collectors.toList());
            
            // 构建返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("list", result);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("list", new ArrayList<>());
            errorResponse.put("total", 0);
            errorResponse.put("page", page);
            errorResponse.put("size", size);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取标准答案")
    public ResponseEntity<Map<String, Object>> getAnswerById(@PathVariable("id") Integer id) {
        try {
            return answerService.getStandardAnswerById(id)
                    .map(answer -> {
                        // 将实体对象转换为Map，避免循环引用问题
                        Map<String, Object> answerMap = new HashMap<>();
                        answerMap.put("standardAnswerId", answer.getStandardAnswerId());
                        answerMap.put("answer", answer.getAnswer());
                        answerMap.put("sourceType", answer.getSourceType());
                        answerMap.put("sourceId", answer.getSourceId());
                        answerMap.put("selectionReason", answer.getSelectionReason());
                        answerMap.put("selectedBy", answer.getSelectedBy());
                        answerMap.put("isFinal", answer.getIsFinal());
                        answerMap.put("createdAt", answer.getCreatedAt());
                        answerMap.put("updatedAt", answer.getUpdatedAt());
                        answerMap.put("version", answer.getVersion());
                        
                        // 添加问题ID
                        answerMap.put("standardQuestionId", answer.getStandardQuestionId());
                        
                        // 添加问题对象信息（可选）
                        if (answer.getStandardQuestion() != null) {
                            Map<String, Object> questionMap = new HashMap<>();
                            questionMap.put("standardQuestionId", answer.getStandardQuestion().getStandardQuestionId());
                            answerMap.put("standardQuestion", questionMap);
                        }
                        
                        return ResponseEntity.ok(answerMap);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "获取问题的所有标准答案")
    public ResponseEntity<List<Map<String, Object>>> getAnswersByQuestionId(
            @PathVariable("questionId") Integer questionId) {
        try {
            log.info("获取问题的所有标准答案，问题ID: {}", questionId);
            List<StandardAnswer> answers = answerService.getStandardAnswersByQuestionId(questionId);
            
            // 打印每个答案的isFinal状态
            for (StandardAnswer answer : answers) {
                log.info("答案ID: {}, isFinal: {}", answer.getStandardAnswerId(), answer.getIsFinal());
            }
            
            // 将实体对象转换为Map，避免循环引用问题
            List<Map<String, Object>> result = answers.stream().map(answer -> {
                Map<String, Object> answerMap = new HashMap<>();
                answerMap.put("standardQuestionId", answer.getStandardQuestionId());
                answerMap.put("standardAnswerId", answer.getStandardAnswerId());
                answerMap.put("answer", answer.getAnswer());
                answerMap.put("sourceType", answer.getSourceType());
                answerMap.put("sourceId", answer.getSourceId());
                answerMap.put("selectionReason", answer.getSelectionReason());
                answerMap.put("selectedBy", answer.getSelectedBy());
                answerMap.put("isFinal", answer.getIsFinal());
                answerMap.put("createdAt", answer.getCreatedAt());
                answerMap.put("updatedAt", answer.getUpdatedAt());
                answerMap.put("version", answer.getVersion());
                
                // 获取并处理关键点
                List<AnswerKeyPoint> keyPoints = answerService.getKeyPointsByAnswerId(answer.getStandardAnswerId());
                log.info("答案ID: {}, 关键点数量: {}", answer.getStandardAnswerId(), keyPoints.size());
                
                if (keyPoints != null && !keyPoints.isEmpty()) {
                    List<Map<String, Object>> keyPointsList = keyPoints.stream().map(kp -> {
                        Map<String, Object> keyPointMap = new HashMap<>();
                        keyPointMap.put("keyPointId", kp.getKeyPointId());
                        keyPointMap.put("pointText", kp.getPointText());
                        keyPointMap.put("pointOrder", kp.getPointOrder());
                        keyPointMap.put("pointWeight", kp.getPointWeight());
                        keyPointMap.put("pointType", kp.getPointType());
                        keyPointMap.put("exampleText", kp.getExampleText());
                        log.debug("关键点ID: {}, 内容: {}, 顺序: {}", 
                                 kp.getKeyPointId(), kp.getPointText(), kp.getPointOrder());
                        return keyPointMap;
                    }).collect(Collectors.toList());
                    answerMap.put("keyPoints", keyPointsList);
                    log.info("处理关键点完成，数量: {}", keyPointsList.size());
                } else {
                    answerMap.put("keyPoints", new ArrayList<>());
                    log.info("该答案没有关键点");
                }
                
                return answerMap;
            }).collect(Collectors.toList());
            
            log.info("获取到标准答案数量: {}", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取问题的所有标准答案失败，问题ID: {}, 错误: {}", questionId, e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/question/{questionId}/final")
    @Operation(summary = "获取问题的最终标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> getFinalAnswerByQuestionId(
            @PathVariable("questionId") Integer questionId) {
        return answerService.getFinalStandardAnswerByQuestionId(questionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "创建新标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> createAnswer(
            @Valid @RequestBody com.llm.eval.model.StandardAnswer answer) {
        com.llm.eval.model.StandardAnswer createdAnswer = answerService.createStandardAnswer(answer);        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnswer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> updateAnswer(
            @PathVariable("id") Integer id,
            @Valid @RequestBody com.llm.eval.model.StandardAnswer answer) {
        try {
            com.llm.eval.model.StandardAnswer updatedAnswer = answerService.updateStandardAnswer(id, answer);
            return ResponseEntity.ok(updatedAnswer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标准答案")
    public ResponseEntity<Void> deleteAnswer(@PathVariable("id") Integer id) {
        try {
            answerService.deleteStandardAnswer(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/set-final")
    @Operation(summary = "设置为最终标准答案")
    public ResponseEntity<com.llm.eval.model.StandardAnswer> setAsFinalAnswer(@PathVariable("id") Integer id) {
        try {
            System.out.println("设置标准答案为最终版本，ID: " + id);
            com.llm.eval.model.StandardAnswer finalAnswer = answerService.markAnswerAsFinal(id);
            System.out.println("设置成功，标准答案ID: " + finalAnswer.getStandardAnswerId());
            return ResponseEntity.ok(finalAnswer);
        } catch (Exception e) {
            System.err.println("设置标准答案为最终版本失败，ID: " + id + ", 错误: " + e.getMessage());
            e.printStackTrace();
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
            // 创建StandardAnswer对象并设置ID
            StandardAnswer answer = new StandardAnswer();
            answer.setStandardAnswerId(answerId);
            keyPoint.setStandardAnswer(answer);
            
            // 调用服务添加关键点
            AnswerKeyPoint createdKeyPoint = answerService.addKeyPoint(keyPoint);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdKeyPoint);
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("添加关键点失败，答案ID: " + answerId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/{id}/key-points")
    @Operation(summary = "获取答案的所有关键点")
    public ResponseEntity<List<Map<String, Object>>> getKeyPointsByAnswerId(
            @PathVariable("id") Integer answerId) {
        try {
            List<AnswerKeyPoint> keyPoints = answerService.getKeyPointsByAnswerId(answerId);
            
            // 将实体对象转换为Map，避免循环引用问题
            List<Map<String, Object>> result = keyPoints.stream().map(kp -> {
                Map<String, Object> map = new HashMap<>();
                map.put("keyPointId", kp.getKeyPointId());
                map.put("pointText", kp.getPointText());
                map.put("pointOrder", kp.getPointOrder());
                map.put("pointWeight", kp.getPointWeight());
                map.put("pointType", kp.getPointType());
                map.put("exampleText", kp.getExampleText());
                map.put("createdAt", kp.getCreatedAt());
                map.put("updatedAt", kp.getUpdatedAt());
                map.put("version", kp.getVersion());
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @PutMapping("/key-points/{keyPointId}")
    @Operation(summary = "更新关键点")
    public ResponseEntity<com.llm.eval.model.AnswerKeyPoint> updateKeyPoint(
            @PathVariable("keyPointId") Integer keyPointId,
            @Valid @RequestBody com.llm.eval.model.AnswerKeyPoint keyPoint) {
        try {
            AnswerKeyPoint updatedKeyPoint = answerService.updateKeyPoint(keyPointId, keyPoint);
            return ResponseEntity.ok(updatedKeyPoint);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @DeleteMapping("/key-points/{keyPointId}")
    @Operation(summary = "删除关键点")
    public ResponseEntity<Void> deleteKeyPoint(
            @PathVariable("keyPointId") Integer keyPointId) {
        try {
            answerService.deleteKeyPoint(keyPointId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 