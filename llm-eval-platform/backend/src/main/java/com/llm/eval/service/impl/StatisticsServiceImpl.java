package com.llm.eval.service.impl;

import com.llm.eval.model.QuestionCategory;
import com.llm.eval.model.Tag;
import com.llm.eval.repository.*;
import com.llm.eval.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    private final RawQuestionRepository rawQuestionRepository;
    private final RawAnswerRepository rawAnswerRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    private final StandardAnswerRepository standardAnswerRepository;
    private final QuestionCategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    
    @Autowired
    public StatisticsServiceImpl(
            RawQuestionRepository rawQuestionRepository,
            RawAnswerRepository rawAnswerRepository,
            StandardQuestionRepository standardQuestionRepository,
            StandardAnswerRepository standardAnswerRepository,
            QuestionCategoryRepository categoryRepository,
            TagRepository tagRepository,
            DatasetVersionRepository datasetVersionRepository) {
        this.rawQuestionRepository = rawQuestionRepository;
        this.rawAnswerRepository = rawAnswerRepository;
        this.standardQuestionRepository = standardQuestionRepository;
        this.standardAnswerRepository = standardAnswerRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.datasetVersionRepository = datasetVersionRepository;
    }
    
    @Override
    public Map<String, Long> getRawQuestionAndAnswerCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("rawQuestionCount", rawQuestionRepository.countRawQuestions());
        counts.put("rawAnswerCount", rawAnswerRepository.countRawAnswers());
        return counts;
    }
    
    @Override
    public Map<String, Long> getStandardQuestionAndAnswerCounts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("standardQuestionCount", standardQuestionRepository.countStandardQuestions());
        counts.put("standardAnswerCount", standardAnswerRepository.countStandardAnswers());
        counts.put("finalStandardAnswerCount", standardAnswerRepository.countFinalStandardAnswers());
        return counts;
    }
    
    @Override
    public Map<String, Long> getQuestionCountsByCategory() {
        Map<String, Long> counts = new HashMap<>();
        List<QuestionCategory> categories = categoryRepository.findAll();
        
        for (QuestionCategory category : categories) {
            long count = standardQuestionRepository.countByCategoryId(category.getCategoryId());
            counts.put(category.getName(), count);
        }
        
        return counts;
    }
    
    @Override
    public Map<String, Long> getQuestionCountsByTag() {
        Map<String, Long> counts = new HashMap<>();
        List<Tag> tags = tagRepository.findAll();
        
        for (Tag tag : tags) {
            long count = standardQuestionRepository.countByTagId(tag.getTagId());
            counts.put(tag.getTagName(), count);
        }
        
        return counts;
    }
    
    @Override
    public long getQuestionsWithoutStandardAnswerCount() {
        return standardQuestionRepository.countWithoutStandardAnswers();
    }
    
    @Override
    public long getDatasetVersionCount() {
        return datasetVersionRepository.countDatasetVersions();
    }
} 