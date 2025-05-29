package com.llm.eval.service;

import com.llm.eval.dto.EvaluationDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EvaluationService {
    
    List<EvaluationDTO> getAllEvaluations();
    
    Optional<EvaluationDTO> getEvaluationById(Integer evaluationId);
    
    List<EvaluationDTO> getEvaluationsByAnswerId(Integer answerId);
    
    List<EvaluationDTO> getEvaluationsByBatchId(Integer batchId);
    
    EvaluationDTO createEvaluation(EvaluationDTO evaluationDTO);
    
    Optional<EvaluationDTO> updateEvaluation(Integer evaluationId, EvaluationDTO evaluationDTO);
    
    boolean deleteEvaluation(Integer evaluationId);
    
    BigDecimal calculateAverageScoreByBatch(Integer batchId);
    
    Long countPassingEvaluationsByBatch(Integer batchId, BigDecimal threshold);
    
    List<EvaluationDTO> evaluateBatch(Integer batchId);
} 