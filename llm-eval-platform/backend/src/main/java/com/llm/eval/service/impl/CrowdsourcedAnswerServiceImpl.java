package com.llm.eval.service.impl;

import com.llm.eval.model.CrowdsourcedAnswer;
import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.User;
import com.llm.eval.repository.CrowdsourcedAnswerRepository;
import com.llm.eval.repository.StandardAnswerRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.UserRepository;
import com.llm.eval.service.CrowdsourcedAnswerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CrowdsourcedAnswerServiceImpl implements CrowdsourcedAnswerService {
    
    private final CrowdsourcedAnswerRepository crowdsourcedAnswerRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    private final StandardAnswerRepository standardAnswerRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public CrowdsourcedAnswerServiceImpl(
            CrowdsourcedAnswerRepository crowdsourcedAnswerRepository,
            StandardQuestionRepository standardQuestionRepository,
            StandardAnswerRepository standardAnswerRepository,
            UserRepository userRepository) {
        this.crowdsourcedAnswerRepository = crowdsourcedAnswerRepository;
        this.standardQuestionRepository = standardQuestionRepository;
        this.standardAnswerRepository = standardAnswerRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public List<CrowdsourcedAnswer> getAllCrowdsourcedAnswers() {
        return crowdsourcedAnswerRepository.findAll();
    }
    
    @Override
    public Optional<CrowdsourcedAnswer> getCrowdsourcedAnswerById(Integer id) {
        return crowdsourcedAnswerRepository.findById(id);
    }
    
    @Override
    public List<CrowdsourcedAnswer> getCrowdsourcedAnswersByQuestionId(Integer questionId) {
        // Verify that the question exists
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        return crowdsourcedAnswerRepository.findByStandardQuestionStandardQuestionId(questionId);
    }
    
    @Override
    public List<CrowdsourcedAnswer> getCrowdsourcedAnswersByQuestionIdAndStatus(
            Integer questionId, CrowdsourcedAnswer.ReviewStatus status) {
        // Verify that the question exists
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        return crowdsourcedAnswerRepository.findByStandardQuestionStandardQuestionIdAndReviewStatus(
                questionId, status);
    }
    
    @Override
    @Transactional
    public CrowdsourcedAnswer createCrowdsourcedAnswer(CrowdsourcedAnswer crowdsourcedAnswer) {
        // Set default values
        if (crowdsourcedAnswer.getCreatedAt() == null) {
            crowdsourcedAnswer.setCreatedAt(LocalDateTime.now());
        }
        crowdsourcedAnswer.setUpdatedAt(LocalDateTime.now());
        crowdsourcedAnswer.setSubmissionTime(LocalDateTime.now());
        
        // Default to pending review
        if (crowdsourcedAnswer.getReviewStatus() == null) {
            crowdsourcedAnswer.setReviewStatus(CrowdsourcedAnswer.ReviewStatus.pending);
        }
        
        // Default to not selected
        if (crowdsourcedAnswer.getIsSelected() == null) {
            crowdsourcedAnswer.setIsSelected(false);
        }
        
        // Verify that the question exists
        Integer questionId = crowdsourcedAnswer.getStandardQuestion().getStandardQuestionId();
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        // Verify that the user exists if provided
        if (crowdsourcedAnswer.getUser() != null && crowdsourcedAnswer.getUser().getUserId() != null) {
            Integer userId = crowdsourcedAnswer.getUser().getUserId();
            if (!userRepository.existsById(userId)) {
                throw new EntityNotFoundException("User not found with id: " + userId);
            }
        }
        
        return crowdsourcedAnswerRepository.save(crowdsourcedAnswer);
    }
    
    @Override
    @Transactional
    public CrowdsourcedAnswer updateCrowdsourcedAnswer(Integer id, CrowdsourcedAnswer crowdsourcedAnswer) {
        CrowdsourcedAnswer existingAnswer = crowdsourcedAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crowdsourced answer not found with id: " + id));
        
        // Cannot update a reviewed answer
        if (existingAnswer.getReviewStatus() != CrowdsourcedAnswer.ReviewStatus.pending) {
            throw new IllegalStateException("Cannot update an answer that has already been reviewed");
        }
        
        // Update properties
        existingAnswer.setAnswerText(crowdsourcedAnswer.getAnswerText());
        existingAnswer.setUpdatedAt(LocalDateTime.now());
        
        return crowdsourcedAnswerRepository.save(existingAnswer);
    }
    
    @Override
    @Transactional
    public CrowdsourcedAnswer reviewCrowdsourcedAnswer(
            Integer id, CrowdsourcedAnswer.ReviewStatus status, Integer reviewerId, String reviewComment) {
        CrowdsourcedAnswer answer = crowdsourcedAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crowdsourced answer not found with id: " + id));
        
        // Verify that the reviewer exists
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new EntityNotFoundException("Reviewer not found with id: " + reviewerId));
        
        // Update review information
        answer.setReviewStatus(status);
        answer.setReviewer(reviewer);
        answer.setReviewTime(LocalDateTime.now());
        answer.setReviewComment(reviewComment);
        answer.setUpdatedAt(LocalDateTime.now());
        
        return crowdsourcedAnswerRepository.save(answer);
    }
    
    @Override
    @Transactional
    public CrowdsourcedAnswer selectCrowdsourcedAnswerAsStandard(Integer id) {
        CrowdsourcedAnswer answer = crowdsourcedAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crowdsourced answer not found with id: " + id));
        
        // Can only select approved answers
        if (answer.getReviewStatus() != CrowdsourcedAnswer.ReviewStatus.approved) {
            throw new IllegalStateException("Only approved answers can be selected as standard");
        }
        
        // Mark this answer as selected
        answer.setIsSelected(true);
        answer.setUpdatedAt(LocalDateTime.now());
        
        // Create a standard answer from this crowdsourced answer
        StandardAnswer standardAnswer = new StandardAnswer();
        standardAnswer.setStandardQuestion(answer.getStandardQuestion());
        standardAnswer.setAnswer(answer.getAnswerText());
        standardAnswer.setSourceType(StandardAnswer.SourceType.crowdsourced);
        standardAnswer.setSourceId(answer.getAnswerId());
        standardAnswer.setSelectionReason("Selected from crowdsourced answer");
        standardAnswer.setSelectedBy(answer.getReviewer() != null ? answer.getReviewer().getUserId() : null);
        standardAnswer.setIsFinal(false); // Not final by default
        standardAnswer.setCreatedAt(LocalDateTime.now());
        standardAnswer.setUpdatedAt(LocalDateTime.now());
        standardAnswer.setVersion(1);
        
        standardAnswerRepository.save(standardAnswer);
        
        return crowdsourcedAnswerRepository.save(answer);
    }
    
    @Override
    @Transactional
    public void deleteCrowdsourcedAnswer(Integer id) {
        crowdsourcedAnswerRepository.deleteById(id);
    }
    
    @Override
    public long countByReviewStatus(CrowdsourcedAnswer.ReviewStatus status) {
        return crowdsourcedAnswerRepository.countByReviewStatus(status);
    }
} 