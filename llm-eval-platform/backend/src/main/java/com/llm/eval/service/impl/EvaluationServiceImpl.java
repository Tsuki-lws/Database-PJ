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
} 