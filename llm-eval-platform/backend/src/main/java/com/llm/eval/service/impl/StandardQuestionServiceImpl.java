package com.llm.eval.service.impl;

import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.Tag;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.TagRepository;
import com.llm.eval.service.StandardQuestionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StandardQuestionServiceImpl implements StandardQuestionService {
    
    private final StandardQuestionRepository standardQuestionRepository;
    private final TagRepository tagRepository;
    
    @Autowired
    public StandardQuestionServiceImpl(
            StandardQuestionRepository standardQuestionRepository,
            TagRepository tagRepository) {
        this.standardQuestionRepository = standardQuestionRepository;
        this.tagRepository = tagRepository;
    }
    
    @Override
    public List<StandardQuestion> getAllStandardQuestions() {
        return standardQuestionRepository.findAll();
    }
    
    @Override
    public Optional<StandardQuestion> getStandardQuestionById(Integer id) {
        return standardQuestionRepository.findById(id);
    }
    
    @Override
    public List<StandardQuestion> getStandardQuestionsByCategoryId(Integer categoryId) {
        return standardQuestionRepository.findByCategoryCategoryId(categoryId);
    }
    
    @Override
    public List<StandardQuestion> getStandardQuestionsByTagId(Integer tagId) {
        return standardQuestionRepository.findByTagId(tagId);
    }
    
    @Override
    public List<StandardQuestion> getQuestionsWithoutStandardAnswers() {
        return standardQuestionRepository.findWithoutStandardAnswers();
    }
    
    @Override
    @Transactional
    public StandardQuestion createStandardQuestion(StandardQuestion standardQuestion) {
        // Set default values
        if (standardQuestion.getCreatedAt() == null) {
            standardQuestion.setCreatedAt(LocalDateTime.now());
        }
        standardQuestion.setUpdatedAt(LocalDateTime.now());
        standardQuestion.setVersion(1);
        
        if (standardQuestion.getStatus() == null) {
            standardQuestion.setStatus(StandardQuestion.QuestionStatus.draft);
        }
        
        if (standardQuestion.getQuestionType() == null) {
            standardQuestion.setQuestionType(StandardQuestion.QuestionType.subjective);
        }
        
        return standardQuestionRepository.save(standardQuestion);
    }
    
    @Override
    @Transactional
    public StandardQuestion updateStandardQuestion(Integer id, StandardQuestion standardQuestion) {
        StandardQuestion existingQuestion = standardQuestionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + id));
        
        // Update properties
        existingQuestion.setQuestion(standardQuestion.getQuestion());
        existingQuestion.setCategory(standardQuestion.getCategory());
        existingQuestion.setQuestionType(standardQuestion.getQuestionType());
        existingQuestion.setDifficulty(standardQuestion.getDifficulty());
        existingQuestion.setSourceQuestion(standardQuestion.getSourceQuestion());
        existingQuestion.setStatus(standardQuestion.getStatus());
        existingQuestion.setUpdatedAt(LocalDateTime.now());
        existingQuestion.setVersion(existingQuestion.getVersion() + 1);
        
        return standardQuestionRepository.save(existingQuestion);
    }
    
    @Override
    @Transactional
    public StandardQuestion addTagsToQuestion(Integer questionId, Set<Integer> tagIds) {
        StandardQuestion question = standardQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + questionId));
        
        Set<Tag> tagsToAdd = tagRepository.findAllById(tagIds).stream().collect(Collectors.toSet());
        
        // Create a new set if it's null
        if (question.getTags() == null) {
            question.setTags(new HashSet<>());
        }
        
        // Add all tags
        question.getTags().addAll(tagsToAdd);
        question.setUpdatedAt(LocalDateTime.now());
        
        return standardQuestionRepository.save(question);
    }
    
    @Override
    @Transactional
    public StandardQuestion removeTagsFromQuestion(Integer questionId, Set<Integer> tagIds) {
        StandardQuestion question = standardQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + questionId));
        
        // If no tags, nothing to remove
        if (question.getTags() == null || question.getTags().isEmpty()) {
            return question;
        }
        
        // Remove the specified tags
        question.setTags(
                question.getTags().stream()
                        .filter(tag -> !tagIds.contains(tag.getTagId()))
                        .collect(Collectors.toSet())
        );
        
        question.setUpdatedAt(LocalDateTime.now());
        
        return standardQuestionRepository.save(question);
    }
    
    @Override
    @Transactional
    public void deleteStandardQuestion(Integer id) {
        standardQuestionRepository.deleteById(id);
    }
} 