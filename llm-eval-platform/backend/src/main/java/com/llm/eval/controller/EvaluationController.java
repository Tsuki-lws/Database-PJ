package com.llm.eval.controller;

import com.llm.eval.dto.EvaluationDTO;
import com.llm.eval.dto.EvaluationKeyPointDTO;
import com.llm.eval.dto.LlmAnswerDTO;
import com.llm.eval.model.LlmAnswer;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.AnswerKeyPoint;
import com.llm.eval.service.EvaluationService;
import com.llm.eval.service.LlmAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/evaluations")
@Tag(name = "Evaluation API", description = "模型评测结果管理接口")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final LlmAnswerService llmAnswerService;

    @Autowired
    public EvaluationController(EvaluationService evaluationService, LlmAnswerService llmAnswerService) {
        this.evaluationService = evaluationService;
        this.llmAnswerService = llmAnswerService;
    }

    @GetMapping
    @Operation(summary = "获取所有评测结果")
    public ResponseEntity<List<EvaluationDTO>> getAllEvaluations() {
        return ResponseEntity.ok(evaluationService.getAllEvaluations());
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有评测结果（带分页和筛选）")
    public ResponseEntity<?> getAllEvaluationsWithFilters(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "datasetId", required = false) Integer datasetId,
            @RequestParam(value = "method", required = false) String method) {
        
        try {
            // 获取所有评测结果
            List<EvaluationDTO> evaluations = evaluationService.getAllEvaluations();
            
            // 应用筛选条件
            List<EvaluationDTO> filteredEvaluations = evaluations.stream()
                .filter(eval -> modelId == null || (eval.getLlmAnswer() != null && 
                    eval.getLlmAnswer().getModel() != null && 
                    eval.getLlmAnswer().getModel().getModelId().equals(modelId)))
                .filter(eval -> method == null || (eval.getMethod() != null && eval.getMethod().equals(method)))
                .collect(Collectors.toList());
            
            // 手动分页
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, filteredEvaluations.size());
            
            List<EvaluationDTO> pagedEvaluations = fromIndex < filteredEvaluations.size() ? 
                    filteredEvaluations.subList(fromIndex, toIndex) : 
                    List.of();
            
            // 转换为前端需要的格式，确保包含所有必要信息
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (EvaluationDTO eval : pagedEvaluations) {
                Map<String, Object> item = new HashMap<>();
                item.put("evaluationId", eval.getEvaluationId());
                item.put("score", eval.getScore());
                item.put("method", eval.getMethod());
                item.put("comments", eval.getComments());
                item.put("createdAt", eval.getCreatedAt());
                
                // 添加模型信息
                if (eval.getLlmAnswer() != null && eval.getLlmAnswer().getModel() != null) {
                    item.put("modelId", eval.getLlmAnswer().getModel().getModelId());
                    item.put("modelName", eval.getLlmAnswer().getModel().getName());
                    item.put("modelVersion", eval.getLlmAnswer().getModel().getVersion());
                } else {
                    item.put("modelId", null);
                    item.put("modelName", "未知模型");
                    item.put("modelVersion", "");
                }
                
                // 添加问题和数据集信息
                if (eval.getLlmAnswer() != null && eval.getLlmAnswer().getStandardQuestion() != null) {
                    item.put("questionId", eval.getLlmAnswer().getStandardQuestion().getStandardQuestionId());
                    item.put("question", eval.getLlmAnswer().getStandardQuestion().getQuestion());
                    
                    // 数据集信息 - 由于DTO中没有直接的dataset字段，这里使用一个占位符
                    // 实际项目中应该通过服务层获取数据集信息
                    item.put("datasetId", null);
                    item.put("datasetName", "未知数据集");
                    
                    // 分类信息
                    if (eval.getLlmAnswer().getStandardQuestion().getCategoryId() != null) {
                        item.put("categoryId", eval.getLlmAnswer().getStandardQuestion().getCategoryId());
                        item.put("categoryName", ""); // 分类名称需要另外获取
                    } else {
                        item.put("categoryId", null);
                        item.put("categoryName", "未分类");
                    }
                } else {
                    item.put("questionId", null);
                    item.put("question", "");
                    item.put("datasetId", null);
                    item.put("datasetName", "未知数据集");
                    item.put("categoryId", null);
                    item.put("categoryName", "未分类");
                }
                
                resultList.add(item);
            }
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", resultList);
            response.put("totalElements", filteredEvaluations.size());
            response.put("totalPages", (int) Math.ceil((double) filteredEvaluations.size() / size));
            response.put("size", size);
            response.put("number", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取评测结果列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取评测结果")
    public ResponseEntity<?> getEvaluationById(@PathVariable("id") Integer id) {
        try {
            System.out.println("获取评测结果详情，ID: " + id);
            
            Optional<EvaluationDTO> evaluationOpt = evaluationService.getEvaluationById(id);
            if (evaluationOpt.isEmpty()) {
                System.err.println("未找到评测结果，ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            EvaluationDTO evaluation = evaluationOpt.get();
            
            // 获取模型回答
            Optional<LlmAnswer> answerOpt = evaluationService.getLlmAnswerById(evaluation.getLlmAnswerId());
            if (answerOpt.isEmpty()) {
                System.err.println("未找到模型回答，ID: " + evaluation.getLlmAnswerId());
                return ResponseEntity.notFound().build();
            }
            
            LlmAnswer answer = answerOpt.get();
            StandardQuestion question = answer.getStandardQuestion();
            
            // 获取标准答案
            Optional<StandardAnswer> standardAnswerOpt = evaluationService.getStandardAnswerByQuestionId(question.getStandardQuestionId());
            
            // 构建完整的响应数据
            Map<String, Object> result = new HashMap<>();
            // 评测基本信息
            result.put("evaluationId", evaluation.getEvaluationId());
            result.put("score", evaluation.getScore());
            result.put("method", evaluation.getMethod());
            result.put("comments", evaluation.getComments());
            result.put("createdAt", evaluation.getCreatedAt());
            result.put("evaluatedAt", evaluation.getCreatedAt());
            
            // 问题信息
            result.put("questionId", question.getStandardQuestionId());
            result.put("question", question.getQuestion());
            result.put("questionType", question.getQuestionType() != null ? question.getQuestionType().toString() : "UNKNOWN");
            
            // 分类信息
            if (question.getCategory() != null) {
                result.put("category", question.getCategory().getName());
                result.put("categoryId", question.getCategory().getCategoryId());
            } else {
                result.put("category", "未分类");
            }
            
            // 模型信息
            result.put("modelId", answer.getModel().getModelId());
            result.put("modelName", answer.getModel().getName() + " " + (answer.getModel().getVersion() != null ? answer.getModel().getVersion() : ""));
            result.put("modelAnswer", answer.getContent());
            
            // 标准答案信息
            if (standardAnswerOpt.isPresent()) {
                StandardAnswer standardAnswer = standardAnswerOpt.get();
                result.put("standardAnswer", standardAnswer.getAnswer());
                result.put("standardAnswerId", standardAnswer.getStandardAnswerId());
                
                // 添加评分要点
                List<AnswerKeyPoint> keyPoints = evaluationService.getKeyPointsByAnswerId(standardAnswer.getStandardAnswerId());
                if (!keyPoints.isEmpty()) {
                    List<Map<String, Object>> keyPointsList = keyPoints.stream().map(kp -> {
                        Map<String, Object> keyPoint = new HashMap<>();
                        keyPoint.put("pointId", kp.getKeyPointId());
                        keyPoint.put("pointText", kp.getPointText());
                        keyPoint.put("pointType", kp.getPointType() != null ? kp.getPointType().toString() : "UNKNOWN");
                        keyPoint.put("pointWeight", kp.getPointWeight());
                        keyPoint.put("pointOrder", kp.getPointOrder());
                        
                        // 添加匹配状态（如果有）
                        String matchStatus = "missed"; // 默认为未匹配
                        if (evaluation.getKeyPointsEvaluation() != null && !evaluation.getKeyPointsEvaluation().isEmpty()) {
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                List<EvaluationKeyPointDTO> keyPointEvals = mapper.readValue(
                                    evaluation.getKeyPointsEvaluation(),
                                    mapper.getTypeFactory().constructCollectionType(List.class, EvaluationKeyPointDTO.class)
                                );
                                
                                for (EvaluationKeyPointDTO kpEval : keyPointEvals) {
                                    if (kpEval.getKeyPointId().equals(kp.getKeyPointId())) {
                                        matchStatus = kpEval.getStatus().toString().toLowerCase();
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("解析关键点评估数据失败: " + e.getMessage());
                            }
                        }
                        keyPoint.put("matchStatus", matchStatus);
                        
                        return keyPoint;
                    }).collect(Collectors.toList());
                    result.put("keyPoints", keyPointsList);
                } else {
                    result.put("keyPoints", new ArrayList<>());
                }
            } else {
                result.put("standardAnswer", "");
                result.put("keyPoints", new ArrayList<>());
            }
            
            System.out.println("评测结果详情获取成功，返回数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取评测结果详情失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取评测结果详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/answer/{answerId}")
    @Operation(summary = "获取特定回答的评测结果")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByAnswerId(@PathVariable("answerId") Integer answerId) {
        List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByAnswerId(answerId);
        return ResponseEntity.ok(evaluations);
    }

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "获取特定批次的评测结果")
    public ResponseEntity<List<EvaluationDTO>> getEvaluationsByBatchId(@PathVariable("batchId") Integer batchId) {
        List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByBatchId(batchId);
        return ResponseEntity.ok(evaluations);
    }

    @PostMapping
    @Operation(summary = "创建评测结果")
    public ResponseEntity<EvaluationDTO> createEvaluation(@Valid @RequestBody EvaluationDTO evaluationDTO) {
        EvaluationDTO createdEvaluation = evaluationService.createEvaluation(evaluationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvaluation);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新评测结果")
    public ResponseEntity<EvaluationDTO> updateEvaluation(
            @PathVariable("id") Integer id,
            @Valid @RequestBody EvaluationDTO evaluationDTO) {
        return evaluationService.updateEvaluation(id, evaluationDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评测结果")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable("id") Integer id) {
        return evaluationService.deleteEvaluation(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/batch/{batchId}/average")
    @Operation(summary = "获取批次的平均评分")
    public ResponseEntity<BigDecimal> getAverageScoreByBatch(@PathVariable("batchId") Integer batchId) {
        BigDecimal averageScore = evaluationService.calculateAverageScoreByBatch(batchId);
        return ResponseEntity.ok(averageScore);
    }

    @GetMapping("/batch/{batchId}/passing")
    @Operation(summary = "获取通过评测的回答数量")
    public ResponseEntity<Long> getPassingEvaluationsCount(
            @PathVariable("batchId") Integer batchId,
            @RequestParam(value = "threshold", defaultValue = "0.6") BigDecimal threshold) {
        Long passingCount = evaluationService.countPassingEvaluationsByBatch(batchId, threshold);
        return ResponseEntity.ok(passingCount);
    }

    @PostMapping("/batch/{batchId}/run")
    @Operation(summary = "执行批次评测")
    public ResponseEntity<List<EvaluationDTO>> runBatchEvaluation(@PathVariable("batchId") Integer batchId) {
        List<EvaluationDTO> evaluations = evaluationService.evaluateBatch(batchId);
        return ResponseEntity.status(HttpStatus.CREATED).body(evaluations);
    }

    @GetMapping("/unevaluated")
    @Operation(summary = "获取待评测的模型回答")
    public ResponseEntity<Map<String, Object>> getUnevaluatedAnswers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "questionType", required = false) String questionType,
            @RequestParam(value = "categoryId", required = false) Integer categoryId) {
        
        try {
            // 获取未评测的回答列表
            List<LlmAnswer> unevaluatedAnswers = evaluationService.getUnevaluatedAnswers(
                    modelId, questionType, categoryId);
            
            // 手动分页
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, unevaluatedAnswers.size());
            
            List<LlmAnswer> pagedAnswers = fromIndex < unevaluatedAnswers.size() ? 
                    unevaluatedAnswers.subList(fromIndex, toIndex) : 
                    List.of();
            
            // 转换为前端需要的格式
            List<Map<String, Object>> result = pagedAnswers.stream().map(answer -> {
                Map<String, Object> item = new HashMap<>();
                item.put("answerId", answer.getLlmAnswerId());
                item.put("questionId", answer.getStandardQuestion().getStandardQuestionId());
                item.put("question", answer.getStandardQuestion().getQuestion());
                item.put("modelId", answer.getModel().getModelId());
                item.put("modelName", answer.getModel().getName() + " " + answer.getModel().getVersion());
                item.put("status", "pending");
                return item;
            }).collect(Collectors.toList());
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", result);
            response.put("totalElements", unevaluatedAnswers.size());
            response.put("totalPages", (int) Math.ceil((double) unevaluatedAnswers.size() / size));
            response.put("size", size);
            response.put("number", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取待评测回答列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/manual")
    @Operation(summary = "提交人工评测结果")
    public ResponseEntity<?> submitManualEvaluation(@RequestBody Map<String, Object> evaluationData) {
        try {
            Integer answerId = (Integer) evaluationData.get("answerId");
            BigDecimal score = new BigDecimal(evaluationData.get("score").toString());
            String comments = (String) evaluationData.get("comments");
            
            // 创建评测DTO
            EvaluationDTO evaluationDTO = new EvaluationDTO();
            evaluationDTO.setLlmAnswerId(answerId);
            evaluationDTO.setScore(score);
            evaluationDTO.setComments(comments);
            evaluationDTO.setMethod("manual");
            
            // 获取标准答案ID
            Optional<LlmAnswer> answerOpt = evaluationService.getLlmAnswerById(answerId);
            if (answerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "模型回答不存在"));
            }
            
            LlmAnswer answer = answerOpt.get();
            Optional<StandardAnswer> standardAnswerOpt = evaluationService.getStandardAnswerByQuestionId(
                    answer.getStandardQuestion().getStandardQuestionId());
            
            if (standardAnswerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "标准答案不存在"));
            }
            
            evaluationDTO.setStandardAnswerId(standardAnswerOpt.get().getStandardAnswerId());
            
            // 处理关键点评估
            if (evaluationData.containsKey("keyPointsStatus")) {
                @SuppressWarnings("unchecked")
                List<String> keyPointsStatus = (List<String>) evaluationData.get("keyPointsStatus");
                
                // 获取关键点列表
                List<AnswerKeyPoint> keyPoints = evaluationService.getKeyPointsByAnswerId(
                        standardAnswerOpt.get().getStandardAnswerId());
                
                if (keyPoints.size() == keyPointsStatus.size()) {
                    List<EvaluationKeyPointDTO> keyPointEvals = new ArrayList<>();
                    
                    for (int i = 0; i < keyPoints.size(); i++) {
                        EvaluationKeyPointDTO keyPointEval = new EvaluationKeyPointDTO();
                        keyPointEval.setKeyPointId(keyPoints.get(i).getKeyPointId());
                        // 将字符串状态转换为枚举
                        String status = keyPointsStatus.get(i);
                        if ("matched".equalsIgnoreCase(status)) {
                            keyPointEval.setStatus(com.llm.eval.model.EvaluationKeyPoint.KeyPointStatus.MATCHED);
                        } else if ("partial".equalsIgnoreCase(status)) {
                            keyPointEval.setStatus(com.llm.eval.model.EvaluationKeyPoint.KeyPointStatus.PARTIAL);
                        } else {
                            keyPointEval.setStatus(com.llm.eval.model.EvaluationKeyPoint.KeyPointStatus.MISSED);
                        }
                        keyPointEvals.add(keyPointEval);
                    }
                    
                    // 将关键点评估结果转换为JSON字符串
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        evaluationDTO.setKeyPointsEvaluation(objectMapper.writeValueAsString(keyPointEvals));
                    } catch (Exception e) {
                        evaluationDTO.setKeyPointsEvaluation("[]");
                    }
                }
            }
            
            // 保存评测结果
            EvaluationDTO savedEvaluation = evaluationService.createEvaluation(evaluationDTO);
            
            return ResponseEntity.ok(savedEvaluation);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "提交评测结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/detail/{answerId}")
    @Operation(summary = "获取评测详情")
    public ResponseEntity<?> getEvaluationDetail(@PathVariable("answerId") Integer answerId) {
        try {
            System.out.println("获取评测详情，回答ID: " + answerId);
            
            // 获取模型回答
            Optional<LlmAnswer> answerOpt = evaluationService.getLlmAnswerById(answerId);
            if (answerOpt.isEmpty()) {
                System.err.println("未找到模型回答，ID: " + answerId);
                return ResponseEntity.notFound().build();
            }
            
            LlmAnswer answer = answerOpt.get();
            StandardQuestion question = answer.getStandardQuestion();
            
            System.out.println("找到模型回答，问题ID: " + question.getStandardQuestionId() + ", 问题: " + question.getQuestion());
            
            // 获取标准答案 - 确保获取最新的标准答案
            Optional<StandardAnswer> standardAnswerOpt = evaluationService.getLatestStandardAnswerByQuestionId(
                    question.getStandardQuestionId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("answerId", answer.getLlmAnswerId());
            result.put("questionId", question.getStandardQuestionId());
            result.put("question", question.getQuestion());
            result.put("questionType", question.getQuestionType() != null ? question.getQuestionType().toString() : "UNKNOWN");
            result.put("modelId", answer.getModel().getModelId());
            result.put("modelName", answer.getModel().getName() + " " + (answer.getModel().getVersion() != null ? answer.getModel().getVersion() : ""));
            result.put("modelAnswer", answer.getContent());
            
            // 添加标准答案信息
            if (standardAnswerOpt.isPresent()) {
                StandardAnswer standardAnswer = standardAnswerOpt.get();
                System.out.println("找到标准答案，ID: " + standardAnswer.getStandardAnswerId() + 
                                  ", 来源类型: " + standardAnswer.getSourceType() + 
                                  ", 更新时间: " + standardAnswer.getUpdatedAt() + 
                                  ", 版本: " + standardAnswer.getVersion());
                
                result.put("standardAnswer", standardAnswer.getAnswer());
                result.put("standardAnswerId", standardAnswer.getStandardAnswerId());
                result.put("standardAnswerSourceType", standardAnswer.getSourceType() != null ? standardAnswer.getSourceType().toString() : null);
                result.put("standardAnswerVersion", standardAnswer.getVersion());
                result.put("standardAnswerUpdatedAt", standardAnswer.getUpdatedAt());
                
                // 添加评分要点 - 确保获取与标准答案关联的所有关键点
                List<AnswerKeyPoint> keyPoints = evaluationService.getKeyPointsByAnswerId(standardAnswer.getStandardAnswerId());
                if (!keyPoints.isEmpty()) {
                    System.out.println("找到关键点数量: " + keyPoints.size());
                    List<Map<String, Object>> keyPointsList = keyPoints.stream().map(kp -> {
                        Map<String, Object> keyPoint = new HashMap<>();
                        keyPoint.put("pointId", kp.getKeyPointId());
                        keyPoint.put("pointText", kp.getPointText());
                        keyPoint.put("pointType", kp.getPointType() != null ? kp.getPointType().toString() : "UNKNOWN");
                        keyPoint.put("pointWeight", kp.getPointWeight());
                        keyPoint.put("pointOrder", kp.getPointOrder());
                        return keyPoint;
                    }).collect(Collectors.toList());
                    result.put("keyPoints", keyPointsList);
                } else {
                    System.out.println("未找到关键点，创建空列表");
                    result.put("keyPoints", new ArrayList<>());
                }
            } else {
                System.err.println("未找到标准答案，问题ID: " + question.getStandardQuestionId());
                // 即使没有标准答案，也返回空的标准答案信息，避免前端报错
                result.put("standardAnswer", "");
                result.put("keyPoints", new ArrayList<>());
            }
            
            // 添加分类信息
            if (question.getCategory() != null) {
                result.put("categoryId", question.getCategory().getCategoryId());
                result.put("categoryName", question.getCategory().getName());
            } else {
                System.out.println("问题没有分类信息");
            }
            
            System.out.println("评测详情获取成功，返回数据");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("获取评测详情失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取评测详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/model")
    @Operation(summary = "获取模型评测结果列表")
    public ResponseEntity<?> getModelEvaluations(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "question", required = false) String question,
            @RequestParam(value = "evaluationType", required = false) String evaluationType,
            @RequestParam(value = "minScore", required = false) BigDecimal minScore,
            @RequestParam(value = "maxScore", required = false) BigDecimal maxScore) {
        
        try {
            System.out.println("获取模型评测结果列表，参数: " + 
                              "page=" + page + ", size=" + size + 
                              ", modelId=" + modelId + ", question=" + question + 
                              ", evaluationType=" + evaluationType + 
                              ", minScore=" + minScore + ", maxScore=" + maxScore);
            
            // 获取评测结果列表
            List<EvaluationDTO> evaluations = evaluationService.getAllEvaluations();
            
            // 应用筛选条件
            List<EvaluationDTO> filteredEvaluations = evaluations.stream()
                .filter(eval -> modelId == null || (eval.getLlmAnswer() != null && 
                    eval.getLlmAnswer().getModel() != null && 
                    eval.getLlmAnswer().getModel().getModelId().equals(modelId)))
                .filter(eval -> question == null || (eval.getLlmAnswer() != null && 
                    eval.getLlmAnswer().getStandardQuestion() != null && 
                    eval.getLlmAnswer().getStandardQuestion().getQuestion() != null && 
                    eval.getLlmAnswer().getStandardQuestion().getQuestion().contains(question)))
                .filter(eval -> evaluationType == null || (eval.getMethod() != null && eval.getMethod().equals(evaluationType)))
                .filter(eval -> minScore == null || (eval.getScore() != null && eval.getScore().compareTo(minScore) >= 0))
                .filter(eval -> maxScore == null || (eval.getScore() != null && eval.getScore().compareTo(maxScore) <= 0))
                .collect(Collectors.toList());
            
            // 手动分页
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, filteredEvaluations.size());
            
            List<EvaluationDTO> pagedEvaluations = fromIndex < filteredEvaluations.size() ? 
                    filteredEvaluations.subList(fromIndex, toIndex) : 
                    List.of();
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", pagedEvaluations);
            response.put("totalElements", filteredEvaluations.size());
            response.put("totalPages", (int) Math.ceil((double) filteredEvaluations.size() / size));
            response.put("size", size);
            response.put("number", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取模型评测结果列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/model/statistics")
    @Operation(summary = "获取模型评测统计数据")
    public ResponseEntity<?> getModelEvaluationStatistics() {
        try {
            // 获取所有评测结果
            List<EvaluationDTO> evaluations = evaluationService.getAllEvaluations();
            
            // 按模型分组
            Map<Integer, List<EvaluationDTO>> evaluationsByModel = evaluations.stream()
                .filter(eval -> eval.getLlmAnswer() != null && 
                    eval.getLlmAnswer().getModel() != null)
                .collect(Collectors.groupingBy(eval -> eval.getLlmAnswer().getModel().getModelId()));
            
            // 计算每个模型的统计数据
            List<Map<String, Object>> statistics = new ArrayList<>();
            
            evaluationsByModel.forEach((modelId, modelEvaluations) -> {
                Map<String, Object> modelStat = new HashMap<>();
                modelStat.put("modelId", modelId);
                
                // 获取模型名称
                String modelName = modelEvaluations.stream()
                    .map(eval -> {
                        if (eval.getLlmAnswer() != null && eval.getLlmAnswer().getModel() != null) {
                            String name = eval.getLlmAnswer().getModel().getName();
                            String version = eval.getLlmAnswer().getModel().getVersion();
                            if (version != null && !version.isEmpty()) {
                                return name + " " + version;
                            }
                            return name;
                        }
                        return null;
                    })
                    .filter(name -> name != null && !name.isEmpty())
                    .findFirst()
                    .orElse("未知模型");
                
                modelStat.put("modelName", modelName);
                modelStat.put("evaluationCount", modelEvaluations.size());
                
                // 计算平均分
                double avgScore = modelEvaluations.stream()
                    .filter(eval -> eval.getScore() != null)
                    .mapToDouble(eval -> eval.getScore().doubleValue())
                    .average()
                    .orElse(0.0);
                
                modelStat.put("avgScore", avgScore);
                
                // 计算评测维度平均分
                Map<String, Double> dimensions = new HashMap<>();
                dimensions.put("accuracy", 0.0);
                dimensions.put("completeness", 0.0);
                dimensions.put("clarity", 0.0);
                
                // 这里需要根据实际情况计算维度得分
                // 假设评测维度信息存储在评测的comments字段中
                
                modelStat.put("dimensions", dimensions);
                
                statistics.add(modelStat);
            });
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取模型评测统计数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/evaluate")
    @Operation(summary = "对单个回答进行评测")
    public ResponseEntity<?> evaluateAnswer(@RequestBody Map<String, Object> evaluationRequest) {
        try {
            Integer answerId = (Integer) evaluationRequest.get("answerId");
            String method = (String) evaluationRequest.get("method");
            
            if (answerId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "回答ID不能为空"));
            }
            
            // 获取模型回答
            Optional<LlmAnswer> answerOpt = evaluationService.getLlmAnswerById(answerId);
            if (answerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "模型回答不存在"));
            }
            
            LlmAnswer answer = answerOpt.get();
            
            // 获取标准答案
            Optional<StandardAnswer> standardAnswerOpt = evaluationService.getStandardAnswerByQuestionId(
                    answer.getStandardQuestion().getStandardQuestionId());
            
            if (standardAnswerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "标准答案不存在"));
            }
            
            // 创建评测DTO
            EvaluationDTO evaluationDTO = new EvaluationDTO();
            evaluationDTO.setLlmAnswerId(answerId);
            evaluationDTO.setMethod(method != null ? method : "auto");
            evaluationDTO.setStandardAnswerId(standardAnswerOpt.get().getStandardAnswerId());
            
            // 如果是自动评测，计算评分
            if ("auto".equals(evaluationDTO.getMethod())) {
                // 简单示例：随机评分，实际中应该使用文本相似度、关键点匹配等复杂算法
                evaluationDTO.setScore(new BigDecimal(Math.random() * 10).setScale(2, BigDecimal.ROUND_HALF_UP));
                evaluationDTO.setComments("自动评测生成的评分");
            } else {
                // 如果是人工评测，返回需要人工评测的信息
                return ResponseEntity.ok(Map.of(
                    "message", "请进行人工评测",
                    "answerId", answerId,
                    "standardAnswerId", standardAnswerOpt.get().getStandardAnswerId()
                ));
            }
            
            // 保存评测结果
            EvaluationDTO savedEvaluation = evaluationService.createEvaluation(evaluationDTO);
            
            return ResponseEntity.ok(savedEvaluation);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "评测失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "获取特定问题的所有评测结果")
    public ResponseEntity<?> getEvaluationsByQuestionId(
            @PathVariable("questionId") Integer questionId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "modelId", required = false) Integer modelId,
            @RequestParam(value = "method", required = false) String method) {
        
        try {
            System.out.println("获取问题ID为 " + questionId + " 的所有评测结果");
            
            // 先获取问题的所有回答
            List<LlmAnswerDTO> questionAnswerDTOs = llmAnswerService.getAnswersByQuestionId(questionId);
            System.out.println("问题ID " + questionId + " 有 " + questionAnswerDTOs.size() + " 个回答");
            
            if (questionAnswerDTOs.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("content", List.of());
                response.put("totalElements", 0);
                response.put("totalPages", 0);
                response.put("size", size);
                response.put("number", page);
                return ResponseEntity.ok(response);
            }
            
            // 获取这些回答的所有评测结果
            List<Map<String, Object>> resultList = new ArrayList<>();
            
            for (LlmAnswerDTO answerDTO : questionAnswerDTOs) {
                List<EvaluationDTO> answerEvaluations = evaluationService.getEvaluationsByAnswerId(answerDTO.getLlmAnswerId());
                
                // 筛选评测方法
                if (method != null && !method.isEmpty()) {
                    answerEvaluations = answerEvaluations.stream()
                        .filter(eval -> eval.getMethod() != null && eval.getMethod().equals(method))
                        .collect(Collectors.toList());
                }
                
                // 如果没有符合条件的评测结果，跳过这个回答
                if (answerEvaluations.isEmpty()) {
                    continue;
                }
                
                // 筛选模型ID
                if (modelId != null && (answerDTO.getModel() == null || !answerDTO.getModel().getModelId().equals(modelId))) {
                    continue;
                }
                
                // 为每个评测结果创建一个详细的响应项
                for (EvaluationDTO eval : answerEvaluations) {
                    Map<String, Object> item = new HashMap<>();
                    
                    // 评测基本信息
                    item.put("evaluationId", eval.getEvaluationId());
                    item.put("score", eval.getScore());
                    item.put("method", eval.getMethod());
                    item.put("comments", eval.getComments());
                    item.put("createdAt", eval.getCreatedAt());
                    
                    // 添加回答信息
                    item.put("answerId", answerDTO.getLlmAnswerId());
                    item.put("answerContent", answerDTO.getContent());
                    
                    // 添加模型信息
                    if (answerDTO.getModel() != null) {
                        item.put("modelId", answerDTO.getModel().getModelId());
                        item.put("modelName", answerDTO.getModel().getName());
                        item.put("modelVersion", answerDTO.getModel().getVersion());
                    } else {
                        item.put("modelId", null);
                        item.put("modelName", "未知模型");
                        item.put("modelVersion", "");
                    }
                    
                    // 添加问题信息
                    if (answerDTO.getStandardQuestion() != null) {
                        item.put("questionId", answerDTO.getStandardQuestion().getStandardQuestionId());
                        item.put("question", answerDTO.getStandardQuestion().getQuestion());
                    } else {
                        item.put("questionId", questionId);
                        item.put("question", "未知问题");
                    }
                    
                    resultList.add(item);
                }
            }
            
            System.out.println("找到 " + resultList.size() + " 条评测结果");
            
            // 手动分页
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, resultList.size());
            
            List<Map<String, Object>> pagedResults = fromIndex < resultList.size() ? 
                    resultList.subList(fromIndex, toIndex) : 
                    List.of();
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("content", pagedResults);
            response.put("totalElements", resultList.size());
            response.put("totalPages", (int) Math.ceil((double) resultList.size() / size));
            response.put("size", size);
            response.put("number", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取问题评测结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 