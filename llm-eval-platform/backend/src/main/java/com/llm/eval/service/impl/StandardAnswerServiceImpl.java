package com.llm.eval.service.impl;

import com.llm.eval.model.StandardAnswer;
import com.llm.eval.repository.StandardAnswerRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.service.StandardAnswerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StandardAnswerServiceImpl implements StandardAnswerService {
    
    private final StandardAnswerRepository standardAnswerRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    
    @Autowired
    public StandardAnswerServiceImpl(
            StandardAnswerRepository standardAnswerRepository,
            StandardQuestionRepository standardQuestionRepository) {
        this.standardAnswerRepository = standardAnswerRepository;
        this.standardQuestionRepository = standardQuestionRepository;
    }
    
    @Override
    public List<StandardAnswer> getAllStandardAnswers() {
        return standardAnswerRepository.findAll();
    }
    
    @Override
    public Optional<StandardAnswer> getStandardAnswerById(Integer id) {
        return standardAnswerRepository.findById(id);
    }
    
    @Override
    public List<StandardAnswer> getStandardAnswersByQuestionId(Integer questionId) {
        // Verify that the question exists
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        return standardAnswerRepository.findByStandardQuestionStandardQuestionId(questionId);
    }
    
    @Override
    public Optional<StandardAnswer> getFinalStandardAnswerByQuestionId(Integer questionId) {
        // Verify that the question exists
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        return standardAnswerRepository.findByStandardQuestionStandardQuestionIdAndIsFinalTrue(questionId);
    }
    
    @Override
    @Transactional
    public StandardAnswer createStandardAnswer(StandardAnswer standardAnswer) {
        // Set default values
        if (standardAnswer.getCreatedAt() == null) {
            standardAnswer.setCreatedAt(LocalDateTime.now());
        }
        standardAnswer.setUpdatedAt(LocalDateTime.now());
        standardAnswer.setVersion(1);
        
        // Default to not final
        if (standardAnswer.getIsFinal() == null) {
            standardAnswer.setIsFinal(false);
        }
        
        // Verify that the question exists
        Integer questionId = standardAnswer.getStandardQuestion().getStandardQuestionId();
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        return standardAnswerRepository.save(standardAnswer);
    }
    
    @Override
    @Transactional
    public StandardAnswer updateStandardAnswer(Integer id, StandardAnswer standardAnswer) {
        StandardAnswer existingAnswer = standardAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Standard answer not found with id: " + id));
        
        // Update properties
        existingAnswer.setAnswer(standardAnswer.getAnswer());
        existingAnswer.setSourceAnswer(standardAnswer.getSourceAnswer());
        existingAnswer.setSourceType(standardAnswer.getSourceType());
        existingAnswer.setSourceId(standardAnswer.getSourceId());
        existingAnswer.setSelectionReason(standardAnswer.getSelectionReason());
        existingAnswer.setSelectedBy(standardAnswer.getSelectedBy());
        existingAnswer.setUpdatedAt(LocalDateTime.now());
        existingAnswer.setVersion(existingAnswer.getVersion() + 1);
        
        // Don't update isFinal here
        
        return standardAnswerRepository.save(existingAnswer);
    }
    
    @Override
    @Transactional
    public StandardAnswer markAnswerAsFinal(Integer id) {
        StandardAnswer answerToMark = standardAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Standard answer not found with id: " + id));
        
        // Get the question ID
        Integer questionId = answerToMark.getStandardQuestion().getStandardQuestionId();
        
        // Find any existing final answers for this question and unmark them
        Optional<StandardAnswer> existingFinalAnswer = 
                standardAnswerRepository.findByStandardQuestionStandardQuestionIdAndIsFinalTrue(questionId);
        
        existingFinalAnswer.ifPresent(answer -> {
            answer.setIsFinal(false);
            answer.setUpdatedAt(LocalDateTime.now());
            standardAnswerRepository.save(answer);
        });
        
        // Mark the new answer as final
        answerToMark.setIsFinal(true);
        answerToMark.setUpdatedAt(LocalDateTime.now());
        
        return standardAnswerRepository.save(answerToMark);
    }
    
    @Override
    @Transactional
    public void deleteStandardAnswer(Integer id) {
        standardAnswerRepository.deleteById(id);
    }
} 