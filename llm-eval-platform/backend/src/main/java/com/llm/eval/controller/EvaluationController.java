package com.llm.eval.controller;

import com.llm.eval.dto.EvaluationDTO;
import com.llm.eval.dto.EvaluationKeyPointDTO;
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

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取评测结果")
    public ResponseEntity<EvaluationDTO> getEvaluationById(@PathVariable("id") Integer id) {
        return evaluationService.getEvaluationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
                .filter(eval -> modelId == null || (eval.getModelId() != null && eval.getModelId().equals(modelId)))
                .filter(eval -> question == null || (eval.getQuestion() != null && eval.getQuestion().contains(question)))
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
                .filter(eval -> eval.getModelId() != null)
                .collect(Collectors.groupingBy(EvaluationDTO::getModelId));
            
            // 计算每个模型的统计数据
            List<Map<String, Object>> statistics = new ArrayList<>();
            
            evaluationsByModel.forEach((modelId, modelEvaluations) -> {
                Map<String, Object> modelStat = new HashMap<>();
                modelStat.put("modelId", modelId);
                
                // 获取模型名称
                String modelName = modelEvaluations.stream()
                    .map(EvaluationDTO::getModelName)
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
} 