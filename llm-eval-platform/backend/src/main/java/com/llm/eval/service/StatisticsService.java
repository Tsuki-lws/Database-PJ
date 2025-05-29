package com.llm.eval.service;

import java.util.Map;

public interface StatisticsService {
    
    /**
     * Get the count of raw questions and answers
     * 
     * @return Map containing the counts
     */
    Map<String, Long> getRawQuestionAndAnswerCounts();
    
    /**
     * Get the count of standard questions and answers
     * 
     * @return Map containing the counts
     */
    Map<String, Long> getStandardQuestionAndAnswerCounts();
    
    /**
     * Get the count of standard questions by category
     * 
     * @return Map of category name to question count
     */
    Map<String, Long> getQuestionCountsByCategory();
    
    /**
     * Get the count of standard questions by tag
     * 
     * @return Map of tag name to question count
     */
    Map<String, Long> getQuestionCountsByTag();
    
    /**
     * Get the count of standard questions without a standard answer
     * 
     * @return The count of questions without a standard answer
     */
    long getQuestionsWithoutStandardAnswerCount();
    
    /**
     * Get the count of dataset versions
     * 
     * @return The count of dataset versions
     */
    long getDatasetVersionCount();
} 