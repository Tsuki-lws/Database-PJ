package com.llm.eval.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.llm.eval.dto.QueryParams;
import com.llm.eval.dto.RawQuestionConverter;
import com.llm.eval.model.RawQuestion;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.QuestionCategory;
import com.llm.eval.repository.RawQuestionRepository;
import com.llm.eval.service.RawQuestionService;
import com.llm.eval.service.StandardQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
public class RawQuestionServiceImpl implements RawQuestionService {

    private final RawQuestionRepository rawQuestionRepository;
    private final StandardQuestionService standardQuestionService;

    @Autowired
    public RawQuestionServiceImpl(RawQuestionRepository rawQuestionRepository, 
                                StandardQuestionService standardQuestionService) {
        this.rawQuestionRepository = rawQuestionRepository;
        this.standardQuestionService = standardQuestionService;
    }

    @Override
    public PageInfo<RawQuestion> findRawQuestionsByPage(QueryParams queryParams) {
        PageHelper.startPage(queryParams.getPageNum(), queryParams.getPageSize());
        
        // 构建查询条件
        String keyword = queryParams.getKeyword();
        String source = queryParams.getSource();
        
        // 这里简化处理，实际可以通过JPA Specification构建更复杂的查询
        List<RawQuestion> questions;
        if (source != null && !source.isEmpty()) {
            questions = rawQuestionRepository.findAll().stream()
                    .filter(q -> q.getSource() != null && q.getSource().equals(source))
                    .collect(Collectors.toList());
        } else {
            questions = rawQuestionRepository.findAll();
        }
        
        // 如果有关键词，进行过滤
        if (keyword != null && !keyword.isEmpty()) {
            questions = questions.stream()
                    .filter(q -> (q.getQuestionTitle() != null && q.getQuestionTitle().contains(keyword)) 
                              || (q.getQuestionBody() != null && q.getQuestionBody().contains(keyword)))
                    .collect(Collectors.toList());
        }
        
        return new PageInfo<>(questions);
    }

    @Override
    public Optional<RawQuestion> findRawQuestionById(Integer id) {
        return rawQuestionRepository.findById(id);
    }

    @Override
    public RawQuestion saveRawQuestion(RawQuestion rawQuestion) {
        return rawQuestionRepository.save(rawQuestion);
    }

    @Override
    public RawQuestion updateRawQuestion(RawQuestion rawQuestion) {
        // 确保问题存在
        if (!rawQuestionRepository.existsById(rawQuestion.getQuestionId())) {
            throw new RuntimeException("原始问题不存在: " + rawQuestion.getQuestionId());
        }
        return rawQuestionRepository.save(rawQuestion);
    }

    @Override
    public void deleteRawQuestion(Integer id) {
        rawQuestionRepository.deleteById(id);
    }

    @Override
    public List<String> getAllSources() {
        return rawQuestionRepository.findAll().stream()
                .map(RawQuestion::getSource)
                .filter(source -> source != null && !source.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getQuestionCountsBySource() {
        Map<String, Long> result = new HashMap<>();
        
        // 获取所有来源
        List<String> sources = getAllSources();
        
        // 统计每个来源的问题数量
        for (String source : sources) {
            long count = rawQuestionRepository.findAll().stream()
                    .filter(q -> source.equals(q.getSource()))
                    .count();
            result.put(source, count);
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> convertToStandardQuestion(Integer id, RawQuestionConverter converter) {
        // 查询原始问题
        RawQuestion rawQuestion = rawQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("原始问题不存在: " + id));
        
        // 准备标准问题
        StandardQuestion standardQuestion = new StandardQuestion();
        
        // 设置问题文本（使用自定义文本或原始文本）
        if (converter.getCustomQuestion() != null && !converter.getCustomQuestion().isEmpty()) {
            standardQuestion.setQuestion(converter.getCustomQuestion());
        } else {
            standardQuestion.setQuestion(rawQuestion.getQuestionBody());
        }
        
        // 设置分类、难度和类型
        // 查询分类对象
        if (converter.getCategoryId() != null) {
            QuestionCategory category = new QuestionCategory();
            category.setCategoryId(converter.getCategoryId());
            standardQuestion.setCategory(category);
        }
        
        // 设置难度（将字符串转换为枚举）
        if (converter.getDifficulty() != null) {
            try {
                StandardQuestion.DifficultyLevel difficultyLevel = 
                    StandardQuestion.DifficultyLevel.valueOf(converter.getDifficulty());
                standardQuestion.setDifficulty(difficultyLevel);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的难度值: " + converter.getDifficulty());
            }
        }
        
        // 设置问题类型（将字符串转换为枚举）
        if (converter.getQuestionType() != null) {
            try {
                StandardQuestion.QuestionType questionType = 
                    StandardQuestion.QuestionType.valueOf(converter.getQuestionType());
                standardQuestion.setQuestionType(questionType);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的问题类型: " + converter.getQuestionType());
            }
        }
        
        // 设置来源问题关联
        standardQuestion.setSourceQuestion(rawQuestion);
        
        // 设置默认状态
        standardQuestion.setStatus(StandardQuestion.QuestionStatus.draft);
        
        // 保存标准问题
        StandardQuestion savedQuestion = standardQuestionService.createStandardQuestion(standardQuestion);
        
        // 关联标签
        if (converter.getTagIds() != null && !converter.getTagIds().isEmpty()) {
            // 使用接口中的方法添加标签
            standardQuestionService.addTagsToQuestion(savedQuestion.getStandardQuestionId(), 
                new HashSet<>(converter.getTagIds()));
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("standardQuestion", savedQuestion);
        result.put("success", true);
        
        return result;
    }
} 