package com.llm.eval.service.impl;

import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.Tag;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.TagRepository;
import com.llm.eval.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public TagServiceImpl(TagRepository tagRepository, StandardQuestionRepository questionRepository, JdbcTemplate jdbcTemplate) {
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
        this.jdbcTemplate = jdbcTemplate;
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
        existingTag.setColor(tag.getColor()); // 更新颜色属性
        
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
    public Map<String, Object> getTagsWithQuestionCountDetails() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Long> countMap = new HashMap<>();
        List<Map<String, Object>> tagDetails = new ArrayList<>();
        List<Tag> tags = tagRepository.findAll();
        
        for (Tag tag : tags) {
            long count = questionRepository.countByTagId(tag.getTagId());
            countMap.put(tag.getTagName(), count);
            
            Map<String, Object> tagDetail = new HashMap<>();
            tagDetail.put("tagId", tag.getTagId());
            tagDetail.put("tagName", tag.getTagName());
            tagDetail.put("description", tag.getDescription());
            // 使用标签自身的颜色，如果为空则使用默认颜色
            tagDetail.put("color", tag.getColor() != null ? tag.getColor() : "#409EFF");
            tagDetail.put("questionCount", count);
            tagDetails.add(tagDetail);
        }
        
        result.put("counts", countMap);
        result.put("tags", tagDetails);
        
        return result;
    }
    
    @Override
    @Transactional
    public void addTagsToQuestion(Integer questionId, List<Integer> tagIds) {
        // 添加日志记录
        System.out.println("TagServiceImpl: 尝试向问题 " + questionId + " 添加标签: " + tagIds);
        
        // 验证问题是否存在
        if (!questionRepository.existsById(questionId)) {
            System.err.println("TagServiceImpl: 问题不存在，ID: " + questionId);
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        // 验证所有标签是否存在
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            System.err.println("TagServiceImpl: 部分标签不存在");
            throw new EntityNotFoundException("Some tags were not found");
        }
        
        // 直接向关联表中插入记录
        int addedCount = 0;
        for (Integer tagId : tagIds) {
            // 检查关联是否已存在
            String checkSql = "SELECT COUNT(*) FROM standard_question_tags WHERE standard_question_id = ? AND tag_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, questionId, tagId);
            
            if (count == null || count == 0) {
                // 不存在则添加
                try {
                    String insertSql = "INSERT INTO standard_question_tags (standard_question_id, tag_id) VALUES (?, ?)";
                    int rowsAffected = jdbcTemplate.update(insertSql, questionId, tagId);
                    addedCount += rowsAffected;
                } catch (Exception e) {
                    System.err.println("TagServiceImpl: 添加标签关联失败: " + e.getMessage());
                    // 如果是唯一约束冲突，说明关联已存在，忽略错误
                    if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                        System.out.println("TagServiceImpl: 标签关联已存在，忽略");
                    } else {
                        throw e;
                    }
                }
            }
        }
        
        System.out.println("TagServiceImpl: 成功向问题 " + questionId + " 添加了 " + addedCount + " 个标签");
    }
    
    @Override
    @Transactional
    public void removeTagFromQuestion(Integer questionId, Integer tagId) {
        // 添加日志记录
        System.out.println("TagServiceImpl: 尝试从问题 " + questionId + " 中移除标签 " + tagId);
        
        // 验证问题是否存在
        if (!questionRepository.existsById(questionId)) {
            System.err.println("TagServiceImpl: 问题不存在，ID: " + questionId);
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        // 验证标签是否存在
        if (!tagRepository.existsById(tagId)) {
            System.err.println("TagServiceImpl: 标签不存在，ID: " + tagId);
            throw new EntityNotFoundException("Tag not found with id: " + tagId);
        }
        
        // 检查关联是否存在
        String checkSql = "SELECT COUNT(*) FROM standard_question_tags WHERE standard_question_id = ? AND tag_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, questionId, tagId);
        
        if (count == null || count == 0) {
            System.err.println("TagServiceImpl: 问题 " + questionId + " 与标签 " + tagId + " 之间不存在关联");
            throw new IllegalStateException("Tag is not associated with this question");
        }
        
        // 直接删除关联表中的记录
        try {
            String deleteSql = "DELETE FROM standard_question_tags WHERE standard_question_id = ? AND tag_id = ?";
            int rowsAffected = jdbcTemplate.update(deleteSql, questionId, tagId);
            System.out.println("TagServiceImpl: 成功从问题 " + questionId + " 中移除标签 " + tagId + "，影响行数: " + rowsAffected);
        } catch (Exception e) {
            System.err.println("TagServiceImpl: 移除标签关联失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public List<Integer> getQuestionsByTags(List<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("Tag IDs cannot be empty");
        }
        
        // 确保所有标签都存在
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new EntityNotFoundException("Some tags were not found");
        }
        
        // 查询具有所有指定标签的问题
        return questionRepository.findByAllTagIds(tagIds);
    }
} 