package com.llm.eval.service;

import com.llm.eval.model.QuestionCategory;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    
    /**
     * Get all categories
     * 
     * @return List of all categories
     */
    List<QuestionCategory> getAllCategories();
    
    /**
     * Get root categories (those without a parent)
     * 
     * @return List of root categories
     */
    List<QuestionCategory> getRootCategories();
    
    /**
     * Get a category by ID
     * 
     * @param id The ID of the category
     * @return The category if found
     */
    Optional<QuestionCategory> getCategoryById(Integer id);
    
    /**
     * Get child categories of a parent category
     * 
     * @param parentId The ID of the parent category
     * @return List of child categories
     */
    List<QuestionCategory> getChildCategories(Integer parentId);
    
    /**
     * Create a new category
     * 
     * @param category The category to create
     * @return The created category
     */
    QuestionCategory createCategory(QuestionCategory category);
    
    /**
     * Update a category
     * 
     * @param id The ID of the category to update
     * @param category The updated category
     * @return The updated category
     */
    QuestionCategory updateCategory(Integer id, QuestionCategory category);
    
    /**
     * Delete a category
     * 
     * @param id The ID of the category to delete
     */
    void deleteCategory(Integer id);
    
    /**
     * Get categories with their question counts
     * 
     * @return List of categories with question count information
     */
    List<QuestionCategory> getCategoriesWithQuestionCount();
} 