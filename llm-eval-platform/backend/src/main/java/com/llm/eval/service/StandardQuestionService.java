package com.llm.eval.service;

import com.llm.eval.model.StandardQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StandardQuestionService {
    
    /**
     * Get all standard questions
     * 
     * @return List of all standard questions
     */
    List<StandardQuestion> getAllStandardQuestions();
    
    /**
     * Get a standard question by ID
     * 
     * @param id The ID of the standard question
     * @return The standard question if found
     */
    Optional<StandardQuestion> getStandardQuestionById(Integer id);
    
    /**
     * Get standard questions by category ID
     * 
     * @param categoryId The ID of the category
     * @return List of standard questions in the category
     */
    List<StandardQuestion> getStandardQuestionsByCategoryId(Integer categoryId);
    
    /**
     * Get standard questions by tag ID
     * 
     * @param tagId The ID of the tag
     * @return List of standard questions with the tag
     */
    List<StandardQuestion> getStandardQuestionsByTagId(Integer tagId);
      /**
     * Get standard questions without a standard answer
     * 
     * @return List of standard questions without a standard answer
     */
    List<StandardQuestion> getQuestionsWithoutStandardAnswers();
    
    /**
     * Get standard questions without a standard answer with pagination
     * 
     * @param pageable 分页参数
     * @return Page of standard questions without a standard answer
     */
    Page<StandardQuestion> getQuestionsWithoutStandardAnswers(Pageable pageable);
      /**
     * Get standard questions without a standard answer with pagination and filters
     * 
     * @param categoryId 分类ID (可选)
     * @param questionType 问题类型 (可选)
     * @param difficulty 难度级别 (可选)
     * @param pageable 分页参数
     * @return Page of standard questions without a standard answer
     */
    Page<StandardQuestion> getQuestionsWithoutStandardAnswersWithFilters(
            Integer categoryId,
            StandardQuestion.QuestionType questionType,
            StandardQuestion.DifficultyLevel difficulty,
            Pageable pageable);
    
    /**
     * Create a new standard question
     * 
     * @param standardQuestion The standard question to create
     * @return The created standard question
     */
    StandardQuestion createStandardQuestion(StandardQuestion standardQuestion);
    
    /**
     * Update a standard question
     * 
     * @param id The ID of the standard question to update
     * @param standardQuestion The updated standard question
     * @return The updated standard question
     */
    StandardQuestion updateStandardQuestion(Integer id, StandardQuestion standardQuestion);
    
    /**
     * Add tags to a standard question
     * 
     * @param questionId The ID of the standard question
     * @param tagIds The IDs of the tags to add
     * @return The updated standard question
     */
    StandardQuestion addTagsToQuestion(Integer questionId, Set<Integer> tagIds);
    
    /**
     * Remove tags from a standard question
     * 
     * @param questionId The ID of the standard question
     * @param tagIds The IDs of the tags to remove
     * @return The updated standard question
     */
    StandardQuestion removeTagsFromQuestion(Integer questionId, Set<Integer> tagIds);
    
    /**
     * Update the category of a standard question
     * 
     * @param questionId The ID of the standard question
     * @param categoryId The ID of the new category (can be null to remove category)
     * @return The updated standard question
     */
    StandardQuestion updateQuestionCategory(Integer questionId, Integer categoryId);
    
    /**
     * Delete a standard question
     * 
     * @param id The ID of the standard question to delete
     */
    void deleteStandardQuestion(Integer id);
}