package com.llm.eval.service;

import com.llm.eval.dto.LlmAnswerDTO;

import java.util.List;
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
} 