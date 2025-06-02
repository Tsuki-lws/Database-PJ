package com.llm.eval.service;

import com.github.pagehelper.PageInfo;
import com.llm.eval.dto.QueryParams;
import com.llm.eval.dto.RawQuestionConverter;
import com.llm.eval.model.RawQuestion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 原始问题服务接口
 */
public interface RawQuestionService {
    
    /**
     * 分页查询原始问题
     * @param queryParams 查询参数
     * @return 分页结果
     */
    PageInfo<RawQuestion> findRawQuestionsByPage(QueryParams queryParams);
    
    /**
     * 根据ID查询原始问题
     * @param id 原始问题ID
     * @return 原始问题
     */
    Optional<RawQuestion> findRawQuestionById(Integer id);
    
    /**
     * 保存原始问题
     * @param rawQuestion 原始问题
     * @return 保存后的原始问题
     */
    RawQuestion saveRawQuestion(RawQuestion rawQuestion);
    
    /**
     * 更新原始问题
     * @param rawQuestion 原始问题
     * @return 更新后的原始问题
     */
    RawQuestion updateRawQuestion(RawQuestion rawQuestion);
    
    /**
     * 删除原始问题
     * @param id 原始问题ID
     */
    void deleteRawQuestion(Integer id);
    
    /**
     * 获取所有问题来源
     * @return 来源列表
     */
    List<String> getAllSources();
    
    /**
     * 获取各来源的问题数量
     * @return 来源-数量映射
     */
    Map<String, Long> getQuestionCountsBySource();
    
    /**
     * 将原始问题转换为标准问题
     * @param id 原始问题ID
     * @param converter 转换参数
     * @return 转换结果
     */
    Map<String, Object> convertToStandardQuestion(Integer id, RawQuestionConverter converter);
} 