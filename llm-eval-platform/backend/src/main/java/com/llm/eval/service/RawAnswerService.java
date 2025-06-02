package com.llm.eval.service;

import com.llm.eval.model.RawAnswer;

import java.util.List;
import java.util.Optional;

/**
 * 原始回答服务接口
 */
public interface RawAnswerService {
    
    /**
     * 根据ID查询原始回答
     * @param id 原始回答ID
     * @return 原始回答
     */
    Optional<RawAnswer> findRawAnswerById(Integer id);
    
    /**
     * 根据问题ID查询原始回答列表
     * @param questionId 问题ID
     * @return 回答列表
     */
    List<RawAnswer> findAnswersByQuestionId(Integer questionId);
    
    /**
     * 保存原始回答
     * @param rawAnswer 原始回答
     * @return 保存后的原始回答
     */
    RawAnswer saveRawAnswer(RawAnswer rawAnswer);
    
    /**
     * 更新原始回答
     * @param rawAnswer 原始回答
     * @return 更新后的原始回答
     */
    RawAnswer updateRawAnswer(RawAnswer rawAnswer);
    
    /**
     * 删除原始回答
     * @param id 原始回答ID
     */
    void deleteRawAnswer(Integer id);
    
    /**
     * 获取原始回答总数
     * @return 回答总数
     */
    long countRawAnswers();
    
    /**
     * 获取指定问题的回答数量
     * @param questionId 问题ID
     * @return 回答数量
     */
    long countByQuestionId(Integer questionId);
} 