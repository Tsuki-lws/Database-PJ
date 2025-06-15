package com.llm.eval.service.impl;

import com.llm.eval.model.RawAnswer;
import com.llm.eval.model.RawQuestion;
import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.repository.RawAnswerRepository;
import com.llm.eval.repository.RawQuestionRepository;
import com.llm.eval.repository.StandardAnswerRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.service.RawAnswerService;
import com.llm.eval.service.StandardAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RawAnswerServiceImpl implements RawAnswerService {

    private final RawAnswerRepository rawAnswerRepository;
    private final RawQuestionRepository rawQuestionRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    private final StandardAnswerRepository standardAnswerRepository;
    private final StandardAnswerService standardAnswerService;

    @Autowired
    public RawAnswerServiceImpl(RawAnswerRepository rawAnswerRepository, 
                              RawQuestionRepository rawQuestionRepository,
                              StandardQuestionRepository standardQuestionRepository,
                              StandardAnswerRepository standardAnswerRepository,
                              StandardAnswerService standardAnswerService) {
        this.rawAnswerRepository = rawAnswerRepository;
        this.rawQuestionRepository = rawQuestionRepository;
        this.standardQuestionRepository = standardQuestionRepository;
        this.standardAnswerRepository = standardAnswerRepository;
        this.standardAnswerService = standardAnswerService;
    }

    @Override
    public Optional<RawAnswer> findRawAnswerById(Integer id) {
        return rawAnswerRepository.findById(id);
    }

    @Override
    public List<RawAnswer> findAnswersByQuestionId(Integer questionId) {
        // 这里我们假设RawAnswerRepository有一个根据问题ID查询答案的方法
        // 实际实现中可能需要添加此方法
        return rawAnswerRepository.findByQuestionQuestionId(questionId);
    }

    @Override
    @Transactional
    public RawAnswer saveRawAnswer(RawAnswer rawAnswer) {
        // 优先使用直接设置的questionId
        final Integer questionId;
        
        if (rawAnswer.getQuestionId() != null) {
            questionId = rawAnswer.getQuestionId();
        } else if (rawAnswer.getQuestion() != null && rawAnswer.getQuestion().getQuestionId() != null) {
            questionId = rawAnswer.getQuestion().getQuestionId();
        } else {
            throw new RuntimeException("未指定问题ID");
        }
        
        // 查询问题对象
        final Integer finalQuestionId = questionId;
        RawQuestion question = rawQuestionRepository.findById(finalQuestionId)
                .orElseThrow(() -> new RuntimeException("原始问题不存在: " + finalQuestionId));
        
        // 设置问题关联
        rawAnswer.setQuestion(question);
        
        return rawAnswerRepository.save(rawAnswer);
    }

    @Override
    public RawAnswer updateRawAnswer(RawAnswer rawAnswer) {
        // 确保答案存在
        if (!rawAnswerRepository.existsById(rawAnswer.getAnswerId())) {
            throw new RuntimeException("原始回答不存在: " + rawAnswer.getAnswerId());
        }
        
        Integer questionId = null;
        
        // 优先使用直接设置的questionId
        if (rawAnswer.getQuestionId() != null) {
            questionId = rawAnswer.getQuestionId();
        } else if (rawAnswer.getQuestion() != null && rawAnswer.getQuestion().getQuestionId() != null) {
            questionId = rawAnswer.getQuestion().getQuestionId();
        }
        
        if (questionId != null) {
            // 确保关联的问题存在
            final Integer finalQuestionId = questionId;
            RawQuestion question = rawQuestionRepository.findById(finalQuestionId)
                    .orElseThrow(() -> new RuntimeException("原始问题不存在: " + finalQuestionId));
            
            rawAnswer.setQuestion(question);
        }
        
        return rawAnswerRepository.save(rawAnswer);
    }

    @Override
    public void deleteRawAnswer(Integer id) {
        rawAnswerRepository.deleteById(id);
    }

    @Override
    public long countRawAnswers() {
        return rawAnswerRepository.countRawAnswers();
    }

    @Override
    public long countByQuestionId(Integer questionId) {
        return rawAnswerRepository.countByQuestionId(questionId);
    }
    
    @Override
    @Transactional
    public Map<String, Object> convertToStandardAnswer(Integer rawAnswerId, Integer standardQuestionId, String answer, Boolean isFinal) {
        // 1. 查找原始回答
        RawAnswer rawAnswer = rawAnswerRepository.findById(rawAnswerId)
                .orElseThrow(() -> new RuntimeException("原始回答不存在: " + rawAnswerId));
        
        // 2. 查找标准问题
        StandardQuestion standardQuestion = standardQuestionRepository.findById(standardQuestionId)
                .orElseThrow(() -> new RuntimeException("标准问题不存在: " + standardQuestionId));
        
        // 3. 创建标准答案
        StandardAnswer standardAnswer = new StandardAnswer();
        standardAnswer.setStandardQuestion(standardQuestion);
        standardAnswer.setAnswer(answer);
        standardAnswer.setSourceAnswer(rawAnswer);
        standardAnswer.setSourceType(StandardAnswer.SourceType.raw);
        standardAnswer.setSourceId(rawAnswerId);
        standardAnswer.setIsFinal(isFinal != null ? isFinal : false);
        standardAnswer.setCreatedAt(LocalDateTime.now());
        standardAnswer.setUpdatedAt(LocalDateTime.now());
        standardAnswer.setVersion(1);
        
        // 4. 保存标准答案
        StandardAnswer savedAnswer = standardAnswerRepository.save(standardAnswer);
        
        // 5. 如果设置为最终答案，则需要取消其他最终答案的标记
        if (isFinal != null && isFinal) {
            // 查找该问题的所有其他最终答案
            Optional<StandardAnswer> existingFinalAnswer = standardAnswerRepository.findByStandardQuestionStandardQuestionIdAndIsFinalTrue(standardQuestionId);
            
            // 如果存在其他最终答案，且不是当前保存的答案，则取消其最终标记
            existingFinalAnswer.ifPresent(otherAnswer -> {
                if (!otherAnswer.getStandardAnswerId().equals(savedAnswer.getStandardAnswerId())) {
                    otherAnswer.setIsFinal(false);
                    otherAnswer.setUpdatedAt(LocalDateTime.now());
                    standardAnswerRepository.save(otherAnswer);
                }
            });
        }
        
        // 6. 构建并返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "转换成功");
        result.put("standardAnswerId", savedAnswer.getStandardAnswerId());
        result.put("standardQuestionId", standardQuestionId);
        
        return result;
    }
} 