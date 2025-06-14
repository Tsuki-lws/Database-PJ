package com.llm.eval.service;

import com.llm.eval.dto.EvaluationDTO;
import com.llm.eval.model.LlmAnswer;
import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.AnswerKeyPoint;

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
    
    /**
     * 获取未评测的模型回答列表
     * 
     * @param modelId 模型ID（可选）
     * @param questionType 问题类型（可选）
     * @param categoryId 分类ID（可选）
     * @return 未评测的模型回答列表
     */
    List<LlmAnswer> getUnevaluatedAnswers(Integer modelId, String questionType, Integer categoryId);
    
    /**
     * 根据ID获取模型回答
     * 
     * @param answerId 回答ID
     * @return 模型回答
     */
    Optional<LlmAnswer> getLlmAnswerById(Integer answerId);
    
    /**
     * 根据问题ID获取标准答案
     * 
     * @param questionId 问题ID
     * @return 标准答案
     */
    Optional<StandardAnswer> getStandardAnswerByQuestionId(Integer questionId);
    
    /**
     * 根据标准答案ID获取关键点列表
     * 
     * @param standardAnswerId 标准答案ID
     * @return 关键点列表
     */
    List<AnswerKeyPoint> getKeyPointsByAnswerId(Integer standardAnswerId);
    
    /**
     * 根据问题ID获取最新的标准答案
     * 
     * @param questionId 问题ID
     * @return 最新的标准答案
     */
    Optional<StandardAnswer> getLatestStandardAnswerByQuestionId(Integer questionId);
} 