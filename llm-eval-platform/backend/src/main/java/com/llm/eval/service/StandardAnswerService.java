package com.llm.eval.service;

import com.llm.eval.model.StandardAnswer;

import java.util.List;
import java.util.Optional;

public interface StandardAnswerService {
    
    /**
     * Get all standard answers
     * 
     * @return List of all standard answers
     */
    List<StandardAnswer> getAllStandardAnswers();
    
    /**
     * Get a standard answer by ID
     * 
     * @param id The ID of the standard answer
     * @return The standard answer if found
     */
    Optional<StandardAnswer> getStandardAnswerById(Integer id);
    
    /**
     * Get standard answers by question ID
     * 
     * @param questionId The ID of the standard question
     * @return List of standard answers for the question
     */
    List<StandardAnswer> getStandardAnswersByQuestionId(Integer questionId);
    
    /**
     * Get the final standard answer for a question
     * 
     * @param questionId The ID of the standard question
     * @return The final standard answer if exists
     */
    Optional<StandardAnswer> getFinalStandardAnswerByQuestionId(Integer questionId);
    
    /**
     * Create a new standard answer
     * 
     * @param standardAnswer The standard answer to create
     * @return The created standard answer
     */
    StandardAnswer createStandardAnswer(StandardAnswer standardAnswer);
    
    /**
     * Update a standard answer
     * 
     * @param id The ID of the standard answer to update
     * @param standardAnswer The updated standard answer
     * @return The updated standard answer
     */
    StandardAnswer updateStandardAnswer(Integer id, StandardAnswer standardAnswer);
    
    /**
     * Mark a standard answer as final
     * 
     * @param id The ID of the standard answer to mark as final
     * @return The updated standard answer
     */
    StandardAnswer markAnswerAsFinal(Integer id);
    
    /**
     * Delete a standard answer
     * 
     * @param id The ID of the standard answer to delete
     */
    void deleteStandardAnswer(Integer id);
} 