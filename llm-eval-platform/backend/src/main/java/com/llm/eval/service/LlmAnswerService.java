package com.llm.eval.service;

import com.llm.eval.dto.LlmAnswerDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LlmAnswerService {
    
    List<LlmAnswerDTO> getAllAnswers();
    
    Optional<LlmAnswerDTO> getAnswerById(Integer answerId);
    
    List<LlmAnswerDTO> getAnswersByModelId(Integer modelId);
    
    List<LlmAnswerDTO> getAnswersByQuestionId(Integer questionId);
    
    List<LlmAnswerDTO> getAnswersByBatchId(Integer batchId);
    
    LlmAnswerDTO createAnswer(LlmAnswerDTO answerDTO);
    
    Optional<LlmAnswerDTO> updateAnswer(Integer answerId, LlmAnswerDTO answerDTO);
    
    boolean deleteAnswer(Integer answerId);
    
    List<LlmAnswerDTO> generateAnswersForBatch(Integer batchId);
    
    /**
     * 获取所有包含模型回答的数据集列表
     * @return 数据集列表，包含数据集ID、名称、版本、问题数量、模型回答数量等信息
     */
    List<Map<String, Object>> getDatasetVersionsWithAnswers();
    
    /**
     * 获取特定数据集中的问题和模型回答情况
     * @param datasetId 数据集ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 包含分页信息和问题列表的Map
     */
    Map<String, Object> getQuestionsWithAnswersInDataset(Integer datasetId, int page, int size);
    
    /**
     * 获取特定数据集中的问题和模型回答情况，支持关键词搜索和按回答状态过滤
     * @param datasetId 数据集ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param keyword 问题内容关键词（可选）
     * @param hasAnswer 是否有回答（可选，true表示有回答，false表示无回答，null表示不过滤）
     * @return 包含分页信息和问题列表的Map
     */
    Map<String, Object> getQuestionsWithAnswersInDataset(Integer datasetId, int page, int size, String keyword, Boolean hasAnswer);
    
    /**
     * 获取特定数据集中特定问题的所有模型回答
     * @param datasetId 数据集ID
     * @param questionId 问题ID
     * @return 模型回答列表
     */
    List<LlmAnswerDTO> getModelAnswersForQuestionInDataset(Integer datasetId, Integer questionId);
} 