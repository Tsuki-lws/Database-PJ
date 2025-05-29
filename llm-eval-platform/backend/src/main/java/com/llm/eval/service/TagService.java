package com.llm.eval.service;

import com.llm.eval.model.Tag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TagService {
    
    /**
     * Get all tags
     * 
     * @return List of all tags
     */
    List<Tag> getAllTags();
    
    /**
     * Get a tag by ID
     * 
     * @param id The ID of the tag
     * @return The tag if found
     */
    Optional<Tag> getTagById(Integer id);
    
    /**
     * Get a tag by name
     * 
     * @param name The name of the tag
     * @return The tag if found
     */
    Optional<Tag> getTagByName(String name);
    
    /**
     * Get tags for a question
     * 
     * @param questionId The ID of the question
     * @return List of tags for the question
     */
    List<Tag> getTagsByQuestionId(Integer questionId);
    
    /**
     * Create a new tag
     * 
     * @param tag The tag to create
     * @return The created tag
     */
    Tag createTag(Tag tag);
    
    /**
     * Update a tag
     * 
     * @param id The ID of the tag to update
     * @param tag The updated tag
     * @return The updated tag
     */
    Tag updateTag(Integer id, Tag tag);
    
    /**
     * Delete a tag
     * 
     * @param id The ID of the tag to delete
     */
    boolean deleteTag(Integer id);
    
    /**
     * Get tags with their question counts
     * 
     * @return Map of tag name to question count
     */
    Map<String, Long> getTagsWithQuestionCount();
    
    void addTagsToQuestion(Integer questionId, List<Integer> tagIds);
    
    void removeTagFromQuestion(Integer questionId, Integer tagId);
} 