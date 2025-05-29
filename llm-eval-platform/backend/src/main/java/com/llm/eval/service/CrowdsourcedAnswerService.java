package com.llm.eval.service;

import com.llm.eval.model.CrowdsourcedAnswer;

import java.util.List;
import java.util.Optional;

public interface CrowdsourcedAnswerService {
    
    /**
     * Get all crowdsourced answers
     * 
     * @return List of all crowdsourced answers
     */
    List<CrowdsourcedAnswer> getAllCrowdsourcedAnswers();
    
    /**
     * Get a crowdsourced answer by ID
     * 
     * @param id The ID of the crowdsourced answer
     * @return The crowdsourced answer if found
     */
    Optional<CrowdsourcedAnswer> getCrowdsourcedAnswerById(Integer id);
    
    /**
     * Get crowdsourced answers for a question
     * 
     * @param questionId The ID of the standard question
     * @return List of crowdsourced answers for the question
     */
    List<CrowdsourcedAnswer> getCrowdsourcedAnswersByQuestionId(Integer questionId);
    
    /**
     * Get crowdsourced answers for a question with a specific review status
     * 
     * @param questionId The ID of the standard question
     * @param status The review status
     * @return List of crowdsourced answers for the question with the specified status
     */
    List<CrowdsourcedAnswer> getCrowdsourcedAnswersByQuestionIdAndStatus(
            Integer questionId, CrowdsourcedAnswer.ReviewStatus status);
    
    /**
     * Create a new crowdsourced answer
     * 
     * @param crowdsourcedAnswer The crowdsourced answer to create
     * @return The created crowdsourced answer
     */
    CrowdsourcedAnswer createCrowdsourcedAnswer(CrowdsourcedAnswer crowdsourcedAnswer);
    
    /**
     * Update a crowdsourced answer
     * 
     * @param id The ID of the crowdsourced answer to update
     * @param crowdsourcedAnswer The updated crowdsourced answer
     * @return The updated crowdsourced answer
     */
    CrowdsourcedAnswer updateCrowdsourcedAnswer(Integer id, CrowdsourcedAnswer crowdsourcedAnswer);
    
    /**
     * Review a crowdsourced answer
     * 
     * @param id The ID of the crowdsourced answer to review
     * @param status The review status
     * @param reviewerId The ID of the reviewer
     * @param reviewComment The review comment
     * @return The updated crowdsourced answer
     */
    CrowdsourcedAnswer reviewCrowdsourcedAnswer(
            Integer id, CrowdsourcedAnswer.ReviewStatus status, Integer reviewerId, String reviewComment);
    
    /**
     * Select a crowdsourced answer as a standard answer
     * 
     * @param id The ID of the crowdsourced answer to select
     * @return The updated crowdsourced answer
     */
    CrowdsourcedAnswer selectCrowdsourcedAnswerAsStandard(Integer id);
    
    /**
     * Delete a crowdsourced answer
     * 
     * @param id The ID of the crowdsourced answer to delete
     */
    void deleteCrowdsourcedAnswer(Integer id);
    
    /**
     * Count crowdsourced answers by review status
     * 
     * @param status The review status
     * @return The count of crowdsourced answers with the specified status
     */
    long countByReviewStatus(CrowdsourcedAnswer.ReviewStatus status);
} 