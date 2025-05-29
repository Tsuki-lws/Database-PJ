package com.llm.eval.service;

import com.llm.eval.model.AnswerKeyPoint;
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
    
    /**
     * Add a key point to a standard answer
     * 
     * @param answerId The ID of the standard answer
     * @param keyPoint The key point to add
     * @return The created key point
     */
    AnswerKeyPoint addKeyPoint(Integer answerId, AnswerKeyPoint keyPoint);
    
    /**
     * Get all key points for a standard answer
     * 
     * @param answerId The ID of the standard answer
     * @return List of key points for the answer
     */
    List<AnswerKeyPoint> getKeyPointsByAnswerId(Integer answerId);
    
    /**
     * Update a key point
     * 
     * @param answerId The ID of the standard answer
     * @param keyPointId The ID of the key point to update
     * @param keyPoint The updated key point
     * @return The updated key point
     */
    AnswerKeyPoint updateKeyPoint(Integer answerId, Integer keyPointId, AnswerKeyPoint keyPoint);
    
    /**
     * Delete a key point
     * 
     * @param answerId The ID of the standard answer
     * @param keyPointId The ID of the key point to delete
     */
    void deleteKeyPoint(Integer answerId, Integer keyPointId);
} 