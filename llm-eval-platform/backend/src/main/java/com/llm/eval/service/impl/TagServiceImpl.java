package com.llm.eval.service.impl;

import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.Tag;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.TagRepository;
import com.llm.eval.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {
    
    private final TagRepository tagRepository;
    private final StandardQuestionRepository questionRepository;
    
    @Autowired
    public TagServiceImpl(TagRepository tagRepository, StandardQuestionRepository questionRepository) {
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
    }
    
    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    
    @Override
    public Optional<Tag> getTagById(Integer id) {
        return tagRepository.findById(id);
    }
    
    @Override
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByTagName(name);
    }
    
    @Override
    public List<Tag> getTagsByQuestionId(Integer questionId) {
        // Verify that the question exists
        if (!questionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        return tagRepository.findByQuestionId(questionId);
    }
    
    @Override
    @Transactional
    public Tag createTag(Tag tag) {
        // Check if a tag with the same name already exists
        if (tagRepository.existsByTagName(tag.getTagName())) {
            throw new IllegalArgumentException("A tag with name '" + tag.getTagName() + "' already exists");
        }
        
        return tagRepository.save(tag);
    }
    
    @Override
    @Transactional
    public Tag updateTag(Integer id, Tag tag) {
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
        
        // Check if the new name is already used by another tag
        if (!existingTag.getTagName().equals(tag.getTagName()) &&
                tagRepository.existsByTagName(tag.getTagName())) {
            throw new IllegalArgumentException("A tag with name '" + tag.getTagName() + "' already exists");
        }
        
        // Update properties
        existingTag.setTagName(tag.getTagName());
        existingTag.setDescription(tag.getDescription());
        
        return tagRepository.save(existingTag);
    }
    
    @Override
    @Transactional
    public boolean deleteTag(Integer id) {
        if (!tagRepository.existsById(id)) {
            return false;
        }
        tagRepository.deleteById(id);
        return true;
    }
    
    @Override
    public Map<String, Long> getTagsWithQuestionCount() {
        Map<String, Long> result = new HashMap<>();
        List<Tag> tags = tagRepository.findAll();
        
        for (Tag tag : tags) {
            long count = questionRepository.countByTagId(tag.getTagId());
            result.put(tag.getTagName(), count);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public void addTagsToQuestion(Integer questionId, List<Integer> tagIds) {
        StandardQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + questionId));
        
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new EntityNotFoundException("Some tags were not found");
        }
        
        // 获取现有标签集合
        Set<Tag> existingTags = question.getTags();
        if (existingTags == null) {
            existingTags = tags.stream().collect(Collectors.toSet());
        } else {
            existingTags.addAll(tags);
        }
        
        // 更新问题的标签集合
        question.setTags(existingTags);
        questionRepository.save(question);
    }
    
    @Override
    @Transactional
    public void removeTagFromQuestion(Integer questionId, Integer tagId) {
        StandardQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + questionId));
        
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + tagId));
        
        Set<Tag> tags = question.getTags();
        if (tags != null) {
            boolean removed = tags.remove(tag);
            if (!removed) {
                throw new IllegalStateException("The tag is not associated with this question");
            }
            
            question.setTags(tags);
            questionRepository.save(question);
        } else {
            throw new IllegalStateException("The question has no tags");
        }
    }
} 