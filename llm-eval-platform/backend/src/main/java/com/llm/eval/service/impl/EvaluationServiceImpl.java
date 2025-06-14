package com.llm.eval.service.impl;

import com.llm.eval.dto.EvaluationDTO;
import com.llm.eval.dto.EvaluationKeyPointDTO;
import com.llm.eval.model.*;
import com.llm.eval.repository.EvaluationBatchRepository;
import com.llm.eval.repository.EvaluationRepository;
import com.llm.eval.repository.LlmAnswerRepository;
import com.llm.eval.repository.StandardAnswerRepository;
import com.llm.eval.service.EvaluationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final LlmAnswerRepository llmAnswerRepository;
    private final StandardAnswerRepository standardAnswerRepository;
    private final EvaluationBatchRepository batchRepository;

    @Autowired
    public EvaluationServiceImpl(
            EvaluationRepository evaluationRepository,
            LlmAnswerRepository llmAnswerRepository,
            StandardAnswerRepository standardAnswerRepository,
            EvaluationBatchRepository batchRepository) {
        this.evaluationRepository = evaluationRepository;
        this.llmAnswerRepository = llmAnswerRepository;
        this.standardAnswerRepository = standardAnswerRepository;
        this.batchRepository = batchRepository;
    }

    @Override
    public List<EvaluationDTO> getAllEvaluations() {
        return evaluationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EvaluationDTO> getEvaluationById(Integer evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .map(this::convertToDTO);
    }

    @Override
    public List<EvaluationDTO> getEvaluationsByAnswerId(Integer answerId) {
        Optional<LlmAnswer> answer = llmAnswerRepository.findById(answerId);
        if (answer.isEmpty()) {
            return new ArrayList<>();
        }
        return evaluationRepository.findByLlmAnswer(answer.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EvaluationDTO> getEvaluationsByBatchId(Integer batchId) {
        Optional<EvaluationBatch> batch = batchRepository.findById(batchId);
        if (batch.isEmpty()) {
            return new ArrayList<>();
        }
        return evaluationRepository.findByBatch(batch.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EvaluationDTO createEvaluation(EvaluationDTO evaluationDTO) {
        Evaluation evaluation = convertToEntity(evaluationDTO);
        evaluation = evaluationRepository.save(evaluation);
        return convertToDTO(evaluation);
    }

    @Override
    @Transactional
    public Optional<EvaluationDTO> updateEvaluation(Integer evaluationId, EvaluationDTO evaluationDTO) {
        if (!evaluationRepository.existsById(evaluationId)) {
            return Optional.empty();
        }
        
        Evaluation evaluation = convertToEntity(evaluationDTO);
        evaluation.setEvaluationId(evaluationId);
        evaluation = evaluationRepository.save(evaluation);
        
        return Optional.of(convertToDTO(evaluation));
    }

    @Override
    @Transactional
    public boolean deleteEvaluation(Integer evaluationId) {
        if (!evaluationRepository.existsById(evaluationId)) {
            return false;
        }
        
        evaluationRepository.deleteById(evaluationId);
        return true;
    }

    @Override
    public BigDecimal calculateAverageScoreByBatch(Integer batchId) {
        Optional<EvaluationBatch> batch = batchRepository.findById(batchId);
        if (batch.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return evaluationRepository.calculateAverageScoreByBatch(batch.get());
    }

    @Override
    public Long countPassingEvaluationsByBatch(Integer batchId, BigDecimal threshold) {
        Optional<EvaluationBatch> batch = batchRepository.findById(batchId);
        if (batch.isEmpty()) {
            return 0L;
        }
        return evaluationRepository.countByBatchAndScoreGreaterThanEqual(batch.get(), threshold);
    }

    @Override
    @Transactional
    public List<EvaluationDTO> evaluateBatch(Integer batchId) {
        Optional<EvaluationBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        EvaluationBatch batch = batchOpt.get();
        batch.setStatus(EvaluationBatch.EvaluationStatus.IN_PROGRESS);
        batch.setStartTime(LocalDateTime.now());
        batchRepository.save(batch);
        
        List<LlmAnswer> answers = llmAnswerRepository.findByBatchAndIsFinalTrue(batch);
        List<Evaluation> evaluations = new ArrayList<>();
        
        for (LlmAnswer answer : answers) {
            Optional<StandardAnswer> standardAnswer = standardAnswerRepository.findByStandardQuestionAndIsFinalTrue(answer.getStandardQuestion());
            if (standardAnswer.isEmpty()) {
                continue;
            }
            
            // 根据评测方法选择不同的评测逻辑
            Evaluation evaluation = new Evaluation();
            evaluation.setLlmAnswer(answer);
            evaluation.setStandardAnswer(standardAnswer.get());
            evaluation.setBatch(batch);
            evaluation.setMethod(batch.getEvaluationMethod().toString());
            
            // 根据评测方法生成评分
            if (batch.getEvaluationMethod() == EvaluationBatch.EvaluationMethod.AUTO) {
                // 自动评测逻辑（简单示例）
                evaluation.setScore(calculateAutoScore(answer, standardAnswer.get()));
            } else if (batch.getEvaluationMethod() == EvaluationBatch.EvaluationMethod.JUDGE_MODEL && batch.getJudgeModel() != null) {
                // 裁判模型评测逻辑
                evaluation.setJudgeModel(batch.getJudgeModel());
                evaluation.setScore(calculateJudgeModelScore(answer, standardAnswer.get(), batch.getJudgeModel()));
            }
            
            evaluations.add(evaluationRepository.save(evaluation));
        }
        
        // 更新批次状态
        batch.setStatus(EvaluationBatch.EvaluationStatus.COMPLETED);
        batch.setEndTime(LocalDateTime.now());
        batchRepository.save(batch);
        
        return evaluations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // 自动评测逻辑示例
    private BigDecimal calculateAutoScore(LlmAnswer answer, StandardAnswer standardAnswer) {
        // 简单示例：随机评分，实际中应该使用文本相似度、关键点匹配等复杂算法
        return new BigDecimal(Math.random()).multiply(BigDecimal.valueOf(10)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    // 裁判模型评测逻辑
    private BigDecimal calculateJudgeModelScore(LlmAnswer answer, StandardAnswer standardAnswer, LlmModel judgeModel) {
        // 这里应该调用裁判模型API进行评分，实际中需要实现具体的调用逻辑
        // 简单示例：随机评分
        return new BigDecimal(Math.random()).multiply(BigDecimal.valueOf(10)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    private EvaluationDTO convertToDTO(Evaluation evaluation) {
        EvaluationDTO dto = new EvaluationDTO();
        BeanUtils.copyProperties(evaluation, dto);
        
        if (evaluation.getLlmAnswer() != null) {
            dto.setLlmAnswerId(evaluation.getLlmAnswer().getLlmAnswerId());
        }
        
        if (evaluation.getStandardAnswer() != null) {
            dto.setStandardAnswerId(evaluation.getStandardAnswer().getStandardAnswerId());
        }
        
        if (evaluation.getBatch() != null) {
            dto.setBatchId(evaluation.getBatch().getBatchId());
        }
        
        if (evaluation.getJudgeModel() != null) {
            dto.setJudgeModelId(evaluation.getJudgeModel().getModelId());
        }
        
        return dto;
    }
    
    private Evaluation convertToEntity(EvaluationDTO dto) {
        Evaluation entity = new Evaluation();
        BeanUtils.copyProperties(dto, entity);
        
        if (dto.getLlmAnswerId() != null) {
            llmAnswerRepository.findById(dto.getLlmAnswerId())
                    .ifPresent(entity::setLlmAnswer);
        }
        
        if (dto.getStandardAnswerId() != null) {
            standardAnswerRepository.findById(dto.getStandardAnswerId())
                    .ifPresent(entity::setStandardAnswer);
        }
        
        if (dto.getBatchId() != null) {
            batchRepository.findById(dto.getBatchId())
                    .ifPresent(entity::setBatch);
        }
        
        return entity;
    }

    @Override
    public List<LlmAnswer> getUnevaluatedAnswers(Integer modelId, String questionType, Integer categoryId) {
        // 获取所有模型回答
        List<LlmAnswer> allAnswers = llmAnswerRepository.findAll();
        
        // 过滤已评测的回答
        List<LlmAnswer> unevaluatedAnswers = allAnswers.stream()
                .filter(answer -> {
                    // 检查是否已有评测记录
                    List<Evaluation> evaluations = evaluationRepository.findByLlmAnswer(answer);
                    return evaluations.isEmpty();
                })
                .filter(answer -> {
                    // 根据模型ID过滤
                    if (modelId != null && !answer.getModel().getModelId().equals(modelId)) {
                        return false;
                    }
                    
                    // 根据问题类型过滤
                    if (questionType != null && !answer.getStandardQuestion().getQuestionType().toString().equals(questionType)) {
                        return false;
                    }
                    
                    // 根据分类ID过滤
                    if (categoryId != null && (answer.getStandardQuestion().getCategory() == null || 
                            !answer.getStandardQuestion().getCategory().getCategoryId().equals(categoryId))) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        return unevaluatedAnswers;
    }
    
    @Override
    public Optional<LlmAnswer> getLlmAnswerById(Integer answerId) {
        return llmAnswerRepository.findById(answerId);
    }
    
    @Override
    public Optional<StandardAnswer> getStandardAnswerByQuestionId(Integer questionId) {
        // 记录日志
        System.out.println("获取问题ID为 " + questionId + " 的标准答案");
        
        // 查找与问题关联的所有标准答案
        List<StandardAnswer> answers = standardAnswerRepository.findAll().stream()
                .filter(answer -> {
                    // 检查标准问题ID是否匹配
                    boolean matches = false;
                    if (answer.getStandardQuestion() != null && 
                        answer.getStandardQuestion().getStandardQuestionId() != null) {
                        matches = answer.getStandardQuestion().getStandardQuestionId().equals(questionId);
                    } else if (answer.getStandardQuestionId() != null) {
                        // 直接检查标准问题ID字段
                        matches = answer.getStandardQuestionId().equals(questionId);
                    }
                    return matches;
                })
                .filter(answer -> answer.getIsFinal() != null && answer.getIsFinal())
                .collect(Collectors.toList());
        
        System.out.println("找到 " + answers.size() + " 个标准答案");
        
        // 如果没有找到答案，尝试通过问题ID直接查询
        if (answers.isEmpty()) {
            System.out.println("通过关联关系未找到答案，尝试直接查询");
            answers = standardAnswerRepository.findByStandardQuestionId(questionId);
            System.out.println("直接查询找到 " + answers.size() + " 个标准答案");
        }
        
        // 返回第一个最终答案
        Optional<StandardAnswer> result = answers.isEmpty() ? Optional.empty() : Optional.of(answers.get(0));
        
        if (result.isPresent()) {
            StandardAnswer answer = result.get();
            System.out.println("返回标准答案 ID: " + answer.getStandardAnswerId() + 
                              ", 来源类型: " + answer.getSourceType() +
                              ", 是否最终版: " + answer.getIsFinal());
        } else {
            System.out.println("未找到标准答案");
        }
        
        return result;
    }
    
    @Override
    public List<AnswerKeyPoint> getKeyPointsByAnswerId(Integer standardAnswerId) {
        System.out.println("获取标准答案ID为 " + standardAnswerId + " 的关键点");
        
        Optional<StandardAnswer> standardAnswer = standardAnswerRepository.findById(standardAnswerId);
        if (standardAnswer.isEmpty()) {
            System.out.println("未找到标准答案，返回空关键点列表");
            return new ArrayList<>();
        }
        
        StandardAnswer answer = standardAnswer.get();
        System.out.println("找到标准答案，来源类型: " + answer.getSourceType());
        
        // 尝试从数据库中获取关键点 - 通过标准答案的关联关系
        List<AnswerKeyPoint> keyPoints = new ArrayList<>();
        
        if (answer.getKeyPoints() != null && !answer.getKeyPoints().isEmpty()) {
            System.out.println("从标准答案的关联关系中找到关键点，数量: " + answer.getKeyPoints().size());
            // 对关键点按照顺序排序
            keyPoints = answer.getKeyPoints().stream()
                    .sorted(Comparator.comparing(AnswerKeyPoint::getPointOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } else {
            System.out.println("标准答案没有关联的关键点，尝试通过其他方式查找");
            
            // 如果是众包提升的标准答案，可能需要特殊处理
            if (answer.getSourceType() != null && answer.getSourceType() == StandardAnswer.SourceType.crowdsourced) {
                System.out.println("检测到众包提升的标准答案，源ID: " + answer.getSourceId());
                // 这里可以添加针对众包提升标准答案的特殊处理
                // 例如，从众包答案中提取关键点
            }
            
            // 如果没有找到关键点，可以尝试创建一些默认的关键点
            if (keyPoints.isEmpty() && answer.getAnswer() != null && !answer.getAnswer().isEmpty()) {
                System.out.println("未找到关键点，尝试从答案内容中提取");
                
                // 这里只是一个示例，实际实现应该更复杂
                String[] paragraphs = answer.getAnswer().split("\n\n");
                if (paragraphs.length > 1) {
                    for (int i = 0; i < paragraphs.length; i++) {
                        String paragraph = paragraphs[i].trim();
                        if (!paragraph.isEmpty()) {
                            // 创建一个关键点
                            // 注意：这里只是示例，实际应该保存到数据库
                            AnswerKeyPoint keyPoint = new AnswerKeyPoint();
                            keyPoint.setKeyPointId(i + 1); // 临时ID
                            keyPoint.setPointText(paragraph.length() > 100 ? paragraph.substring(0, 100) + "..." : paragraph);
                            keyPoint.setPointType(AnswerKeyPoint.PointType.required);
                            keyPoint.setPointWeight(new BigDecimal("1.0"));
                            keyPoint.setPointOrder(i);
                            keyPoint.setStandardAnswer(answer);
                            keyPoints.add(keyPoint);
                        }
                    }
                }
            }
        }
        
        System.out.println("返回关键点数量: " + keyPoints.size());
        return keyPoints;
    }

    @Override
    public Optional<StandardAnswer> getLatestStandardAnswerByQuestionId(Integer questionId) {
        System.out.println("获取问题ID为 " + questionId + " 的最新标准答案");
        
        // 查找与问题关联的所有标准答案
        List<StandardAnswer> allAnswers = standardAnswerRepository.findAll().stream()
                .filter(answer -> {
                    // 检查标准问题ID是否匹配
                    boolean matches = false;
                    if (answer.getStandardQuestion() != null && 
                        answer.getStandardQuestion().getStandardQuestionId() != null) {
                        matches = answer.getStandardQuestion().getStandardQuestionId().equals(questionId);
                    } else if (answer.getStandardQuestionId() != null) {
                        // 直接检查标准问题ID字段
                        matches = answer.getStandardQuestionId().equals(questionId);
                    }
                    return matches;
                })
                .collect(Collectors.toList());
        
        System.out.println("找到 " + allAnswers.size() + " 个标准答案");
        
        // 如果没有找到答案，尝试通过问题ID直接查询
        if (allAnswers.isEmpty()) {
            System.out.println("通过关联关系未找到答案，尝试直接查询");
            allAnswers = standardAnswerRepository.findByStandardQuestionId(questionId);
            System.out.println("直接查询找到 " + allAnswers.size() + " 个标准答案");
        }
        
        if (allAnswers.isEmpty()) {
            System.out.println("未找到任何标准答案");
            return Optional.empty();
        }
        
        // 首先尝试找到最终版本的答案
        Optional<StandardAnswer> finalAnswer = allAnswers.stream()
                .filter(answer -> answer.getIsFinal() != null && answer.getIsFinal())
                .max(Comparator.comparing(StandardAnswer::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
        
        if (finalAnswer.isPresent()) {
            StandardAnswer answer = finalAnswer.get();
            System.out.println("找到最终版本的标准答案，ID: " + answer.getStandardAnswerId() + 
                              ", 更新时间: " + answer.getUpdatedAt() + 
                              ", 版本: " + answer.getVersion());
            return finalAnswer;
        }
        
        // 如果没有最终版本，返回最新更新的答案
        Optional<StandardAnswer> latestAnswer = allAnswers.stream()
                .max(Comparator.comparing(StandardAnswer::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
        
        if (latestAnswer.isPresent()) {
            StandardAnswer answer = latestAnswer.get();
            System.out.println("找到最新更新的标准答案，ID: " + answer.getStandardAnswerId() + 
                              ", 更新时间: " + answer.getUpdatedAt() + 
                              ", 版本: " + answer.getVersion());
        } else {
            System.out.println("未找到最新更新的标准答案");
        }
        
        return latestAnswer;
    }
} 