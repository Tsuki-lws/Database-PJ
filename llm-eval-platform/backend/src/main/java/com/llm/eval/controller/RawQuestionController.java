package com.llm.eval.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/raw-questions")
@Tag(name = "Raw Question API", description = "原始问题管理接口")
public class RawQuestionController {

    private static final Logger logger = LoggerFactory.getLogger(RawQuestionController.class);

    private final RawQuestionService rawQuestionService;
    private final RawAnswerService rawAnswerService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public RawQuestionController(RawQuestionService rawQuestionService, RawAnswerService rawAnswerService) {
        this.rawQuestionService = rawQuestionService;
        this.rawAnswerService = rawAnswerService;
    }

    @GetMapping
    @Operation(summary = "获取原始问题列表")
    public ResponseEntity<Map<String, Object>> listRawQuestions(QueryParams queryParams) {
        try {
            logger.debug("接收到获取原始问题列表请求，参数: {}", queryParams);
            
            // 从请求中获取分页参数
            int pageNum = queryParams.getPageNum() != null ? queryParams.getPageNum() : 0; // Spring Data页码从0开始
            int pageSize = queryParams.getPageSize() != null ? queryParams.getPageSize() : 10;
            
            // 创建分页请求
            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            logger.debug("执行分页查询，页码: {}, 每页大小: {}", pageNum, pageSize);
            
            Page<RawQuestion> page = null;
            List<Map<String, Object>> simplifiedList = new ArrayList<>();
            long totalElements = 0;
            int totalPages = 0;
            
            try {
                // 获取分页数据
                page = rawQuestionService.findRawQuestionsByPage(queryParams, pageable);
                logger.debug("查询结果: page={}", page);
                
                // 构建极度精简的响应对象，避免任何可能的循环引用
                if (page != null && page.getContent() != null) {
                    for (RawQuestion question : page.getContent()) {
                        try {
                            if (question != null) {
                                Map<String, Object> item = new HashMap<>();
                                item.put("questionId", question.getQuestionId());
                                
                                // 标题可能为空
                                String title = question.getQuestionTitle();
                                item.put("questionTitle", title != null ? title : "");
                                
                                // 截断问题内容，并确保内容不为空
                                String body = question.getQuestionBody();
                                if (body != null) {
                                    if (body.length() > 200) { // 比之前更短
                                        body = body.substring(0, 200) + "...";
                                    }
                                    item.put("questionBody", body);
                                } else {
                                    item.put("questionBody", "");
                                }
                                
                                // 其他可能为空的字段
                                item.put("source", question.getSource() != null ? question.getSource() : "");
                                
                                // 日期格式化
                                LocalDateTime createdTime = question.getCreatedAt();
                                if (createdTime != null) {
                                    try {
                                        item.put("createdAt", createdTime.format(DATE_FORMATTER));
                                    } catch (Exception e) {
                                        item.put("createdAt", "");
                                        logger.warn("日期格式化失败: {}", e.getMessage());
                                    }
                                } else {
                                    item.put("createdAt", "");
                                }
                                
                                simplifiedList.add(item);
                            }
                        } catch (Exception e) {
                            logger.error("处理单个问题时出错: {}", e.getMessage());
                            // 继续处理下一个问题，不中断整个循环
                        }
                    }
                    
                    // 获取分页数据
                    try {
                        totalElements = page.getTotalElements();
                        totalPages = page.getTotalPages();
                    } catch (Exception e) {
                        logger.error("获取分页信息失败", e);
                    }
                }
            } catch (Exception e) {
                logger.error("执行查询或处理结果时出错", e);
            }
            
            // 构建最终响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", simplifiedList);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("size", page != null ? page.getSize() : pageSize);
            response.put("number", page != null ? page.getNumber() : pageNum);
            
            logger.debug("成功构建响应，返回问题数量: {}", simplifiedList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取原始问题列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("content", new ArrayList<>()); // 确保前端始终可以获取到content字段
            errorResponse.put("totalElements", 0);
            errorResponse.put("totalPages", 0);
            errorResponse.put("size", 10);
            errorResponse.put("number", 0);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取原始问题")
    public ResponseEntity<RawQuestion> getRawQuestion(@PathVariable Integer id) {
        try {
            logger.debug("根据ID获取原始问题，ID: {}", id);
            return rawQuestionService.findRawQuestionById(id)
                    .map(question -> {
                        logger.debug("获取成功，问题ID: {}", id);
                        return ResponseEntity.ok(question);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("根据ID获取原始问题失败，ID: {}", id, e);
            throw new RuntimeException("获取原始问题失败: " + e.getMessage(), e);
        }
    }

    @PostMapping
    @Operation(summary = "创建原始问题")
    public ResponseEntity<RawQuestion> createRawQuestion(@RequestBody RawQuestion rawQuestion) {
        try {
            logger.debug("创建原始问题，内容: {}", rawQuestion);
            RawQuestion saved = rawQuestionService.saveRawQuestion(rawQuestion);
            logger.debug("创建成功，问题ID: {}", saved.getQuestionId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("创建原始问题失败", e);
            throw new RuntimeException("创建原始问题失败: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新原始问题")
    public ResponseEntity<RawQuestion> updateRawQuestion(@PathVariable Integer id, @RequestBody RawQuestion rawQuestion) {
        try {
            logger.debug("更新原始问题，ID: {}, 内容: {}", id, rawQuestion);
            rawQuestion.setQuestionId(id);
            RawQuestion updated = rawQuestionService.updateRawQuestion(rawQuestion);
            logger.debug("更新成功，问题ID: {}", updated.getQuestionId());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("更新原始问题失败，ID: {}", id, e);
            throw new RuntimeException("更新原始问题失败: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除原始问题")
    public ResponseEntity<Void> deleteRawQuestion(@PathVariable Integer id) {
        try {
            logger.debug("删除原始问题，ID: {}", id);
            rawQuestionService.deleteRawQuestion(id);
            logger.debug("删除成功，问题ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("删除原始问题失败，ID: {}", id, e);
            throw new RuntimeException("删除原始问题失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{questionId}/answers")
    @Operation(summary = "获取原始问题的回答列表")
    public ResponseEntity<List<Map<String, Object>>> getRawAnswers(@PathVariable Integer questionId) {
        try {
            logger.debug("获取原始问题的回答列表，问题ID: {}", questionId);
            List<RawAnswer> answers = rawAnswerService.findAnswersByQuestionId(questionId);
            logger.debug("获取成功，问题ID: {}, 回答数量: {}", questionId, answers.size());
            
            // 简化响应，避免循环引用
            List<Map<String, Object>> simplifiedAnswers = new ArrayList<>();
            for (RawAnswer answer : answers) {
                Map<String, Object> item = new HashMap<>();
                item.put("answerId", answer.getAnswerId());
                item.put("answerBody", answer.getAnswerBody());
                item.put("authorInfo", answer.getAuthorInfo());
                item.put("upvotes", answer.getUpvotes());
                item.put("isAccepted", answer.getIsAccepted());
                item.put("questionId", questionId);
                
                // 日期格式化
                if (answer.getCreatedAt() != null) {
                    item.put("createdAt", answer.getCreatedAt().format(DATE_FORMATTER));
                } else {
                    item.put("createdAt", "");
                }
                
                simplifiedAnswers.add(item);
            }
            
            return ResponseEntity.ok(simplifiedAnswers);
        } catch (Exception e) {
            logger.error("获取原始问题的回答列表失败，问题ID: {}", questionId, e);
            throw new RuntimeException("获取回答列表失败: " + e.getMessage(), e);
        }
    }

    @PostMapping("/{questionId}/answers")
    @Operation(summary = "为原始问题添加回答")
    public ResponseEntity<RawAnswer> addRawAnswer(@PathVariable Integer questionId, @RequestBody RawAnswer rawAnswer) {
        try {
            logger.debug("为原始问题添加回答，问题ID: {}, 回答内容: {}", questionId, rawAnswer);
            // 查询问题对象
            RawQuestion question = rawQuestionService.findRawQuestionById(questionId)
                    .orElseThrow(() -> new RuntimeException("原始问题不存在: " + questionId));
            
            // 设置问题关联
            rawAnswer.setQuestion(question);
            rawAnswer.setQuestionId(questionId);
            
            RawAnswer saved = rawAnswerService.saveRawAnswer(rawAnswer);
            logger.debug("添加回答成功，回答ID: {}", saved.getAnswerId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("为原始问题添加回答失败，问题ID: {}", questionId, e);
            throw new RuntimeException("添加回答失败: " + e.getMessage(), e);
        }
    }

    @PostMapping("/{id}/convert")
    @Operation(summary = "将原始问题转换为标准问题")
    public ResponseEntity<Map<String, Object>> convertToStandardQuestion(@PathVariable Integer id, @RequestBody RawQuestionConverter converter) {
        try {
            logger.debug("将原始问题转换为标准问题，原始问题ID: {}, 转换参数: {}", id, converter);
            Map<String, Object> result = rawQuestionService.convertToStandardQuestion(id, converter);
            logger.debug("转换成功，原始问题ID: {}", id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("将原始问题转换为标准问题失败，原始问题ID: {}", id, e);
            throw new RuntimeException("转换标准问题失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/sources")
    @Operation(summary = "获取所有问题来源")
    public ResponseEntity<List<String>> getAllSources() {
        try {
            logger.debug("获取所有问题来源");
            List<String> sources = rawQuestionService.getAllSources();
            logger.debug("获取成功，来源数量: {}", sources.size());
            return ResponseEntity.ok(sources);
        } catch (Exception e) {
            logger.error("获取所有问题来源失败", e);
            throw new RuntimeException("获取问题来源失败: " + e.getMessage(), e);
        }
    }

    @GetMapping("/stats/by-source")
    @Operation(summary = "获取各来源的问题数量")
    public ResponseEntity<Map<String, Long>> getQuestionCountsBySource() {
        try {
            logger.debug("获取各来源的问题数量");
            Map<String, Long> counts = rawQuestionService.getQuestionCountsBySource();
            logger.debug("获取成功，来源数量: {}", counts.size());
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            logger.error("获取各来源的问题数量失败", e);
            throw new RuntimeException("获取问题数量统计失败: " + e.getMessage(), e);
        }
    }
} 