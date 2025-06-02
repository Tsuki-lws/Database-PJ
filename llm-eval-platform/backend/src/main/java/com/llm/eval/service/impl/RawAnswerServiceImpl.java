package com.llm.eval.service.impl;

import com.llm.eval.model.RawAnswer;
import com.llm.eval.model.RawQuestion;
import com.llm.eval.repository.RawAnswerRepository;
import com.llm.eval.repository.RawQuestionRepository;
import com.llm.eval.service.RawAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RawAnswerServiceImpl implements RawAnswerService {

    private final RawAnswerRepository rawAnswerRepository;
    private final RawQuestionRepository rawQuestionRepository;

    @Autowired
    public RawAnswerServiceImpl(RawAnswerRepository rawAnswerRepository, 
                              RawQuestionRepository rawQuestionRepository) {
        this.rawAnswerRepository = rawAnswerRepository;
        this.rawQuestionRepository = rawQuestionRepository;
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
        // 确保关联的问题存在
        RawQuestion question = rawQuestionRepository.findById(rawAnswer.getQuestion().getQuestionId())
                .orElseThrow(() -> new RuntimeException("原始问题不存在: " + rawAnswer.getQuestion().getQuestionId()));
        
        rawAnswer.setQuestion(question);
        return rawAnswerRepository.save(rawAnswer);
    }

    @Override
    public RawAnswer updateRawAnswer(RawAnswer rawAnswer) {
        // 确保答案存在
        if (!rawAnswerRepository.existsById(rawAnswer.getAnswerId())) {
            throw new RuntimeException("原始回答不存在: " + rawAnswer.getAnswerId());
        }
        
        // 确保关联的问题存在
        RawQuestion question = rawQuestionRepository.findById(rawAnswer.getQuestion().getQuestionId())
                .orElseThrow(() -> new RuntimeException("原始问题不存在: " + rawAnswer.getQuestion().getQuestionId()));
        
        rawAnswer.setQuestion(question);
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
} 