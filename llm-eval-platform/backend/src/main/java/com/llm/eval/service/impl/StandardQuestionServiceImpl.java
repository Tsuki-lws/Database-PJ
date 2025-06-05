package com.llm.eval.service.impl;

import com.llm.eval.model.QuestionCategory;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.Tag;
import com.llm.eval.repository.QuestionCategoryRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.TagRepository;
import com.llm.eval.service.StandardQuestionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StandardQuestionServiceImpl implements StandardQuestionService {    private final StandardQuestionRepository standardQuestionRepository;
    private final TagRepository tagRepository;
    private final QuestionCategoryRepository categoryRepository;
    
    public StandardQuestionServiceImpl(
            StandardQuestionRepository standardQuestionRepository,
            TagRepository tagRepository,
            QuestionCategoryRepository categoryRepository) {
        this.standardQuestionRepository = standardQuestionRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    public List<StandardQuestion> getAllStandardQuestions() {
        return standardQuestionRepository.findAll();
    }
    
    @Override
    public Optional<StandardQuestion> getStandardQuestionById(Integer id) {
        return standardQuestionRepository.findById(id);
    }
    
    @Override
    public List<StandardQuestion> getStandardQuestionsByCategoryId(Integer categoryId) {
        return standardQuestionRepository.findByCategoryCategoryId(categoryId);
    }
    
    @Override
    public List<StandardQuestion> getStandardQuestionsByTagId(Integer tagId) {
        return standardQuestionRepository.findByTagId(tagId);
    }
      @Override
    public List<StandardQuestion> getQuestionsWithoutStandardAnswers() {
        return standardQuestionRepository.findWithoutStandardAnswers();
    }
    
    @Override
    public Page<StandardQuestion> getQuestionsWithoutStandardAnswers(Pageable pageable) {
        return standardQuestionRepository.findWithoutStandardAnswers(pageable);
    }
    
    @Override
    public Page<StandardQuestion> getQuestionsWithoutStandardAnswersWithFilters(
            Integer categoryId,
            StandardQuestion.QuestionType questionType,
            StandardQuestion.DifficultyLevel difficulty,
            Pageable pageable) {
        return standardQuestionRepository.findWithoutStandardAnswersWithFilters(
                categoryId, questionType, difficulty, pageable);
    }
    
    @Override
    @Transactional
    public StandardQuestion createStandardQuestion(StandardQuestion standardQuestion) {
        // Set default values
        if (standardQuestion.getCreatedAt() == null) {
            standardQuestion.setCreatedAt(LocalDateTime.now());
        }
        standardQuestion.setUpdatedAt(LocalDateTime.now());
        standardQuestion.setVersion(1);
        
        if (standardQuestion.getStatus() == null) {
            standardQuestion.setStatus(StandardQuestion.QuestionStatus.draft);
        }
        
        if (standardQuestion.getQuestionType() == null) {
            standardQuestion.setQuestionType(StandardQuestion.QuestionType.subjective);
        }
        
        return standardQuestionRepository.save(standardQuestion);
    }
    
    @Override
    @Transactional
    public StandardQuestion updateStandardQuestion(Integer id, StandardQuestion standardQuestion) {
        StandardQuestion existingQuestion = standardQuestionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + id));
        
        // Update properties
        existingQuestion.setQuestion(standardQuestion.getQuestion());
        existingQuestion.setCategory(standardQuestion.getCategory());
        existingQuestion.setQuestionType(standardQuestion.getQuestionType());
        existingQuestion.setDifficulty(standardQuestion.getDifficulty());
        existingQuestion.setSourceQuestion(standardQuestion.getSourceQuestion());
        existingQuestion.setStatus(standardQuestion.getStatus());
        existingQuestion.setUpdatedAt(LocalDateTime.now());
        existingQuestion.setVersion(existingQuestion.getVersion() + 1);
        
        return standardQuestionRepository.save(existingQuestion);
    }
    
    @Override
    @Transactional
    public StandardQuestion addTagsToQuestion(Integer questionId, Set<Integer> tagIds) {
        StandardQuestion question = standardQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + questionId));
        
        Set<Tag> tagsToAdd = tagRepository.findAllById(tagIds).stream().collect(Collectors.toSet());
        
        // Create a new set if it's null
        if (question.getTags() == null) {
            question.setTags(new HashSet<>());
        }
        
        // Add all tags
        question.getTags().addAll(tagsToAdd);
        question.setUpdatedAt(LocalDateTime.now());
        
        return standardQuestionRepository.save(question);
    }
    
    @Override
    @Transactional
    public StandardQuestion removeTagsFromQuestion(Integer questionId, Set<Integer> tagIds) {
        // 添加日志
        System.out.println("StandardQuestionServiceImpl: 尝试从问题 " + questionId + " 中移除标签: " + tagIds);
        
        StandardQuestion question = standardQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + questionId));
        
        // If no tags, nothing to remove
        if (question.getTags() == null || question.getTags().isEmpty()) {
            System.out.println("StandardQuestionServiceImpl: 问题没有标签可移除");
            throw new IllegalStateException("Question has no tags to remove");
        }
        
        // 检查要移除的标签是否存在于问题中
        boolean allTagsExist = tagIds.stream()
                .allMatch(tagId -> question.getTags().stream()
                        .anyMatch(tag -> tag.getTagId().equals(tagId)));
        
        if (!allTagsExist) {
            System.out.println("StandardQuestionServiceImpl: 部分标签不在问题中");
            throw new IllegalStateException("Some tags are not associated with this question");
        }
        
        // 记录移除前的标签数量
        int beforeSize = question.getTags().size();
        
        // Remove the specified tags
        question.setTags(
                question.getTags().stream()
                        .filter(tag -> !tagIds.contains(tag.getTagId()))
                        .collect(Collectors.toSet())
        );
        
        // 记录移除后的标签数量
        int afterSize = question.getTags().size();
        System.out.println("StandardQuestionServiceImpl: 从问题中移除了 " + (beforeSize - afterSize) + " 个标签");
        
        question.setUpdatedAt(LocalDateTime.now());
        
        return standardQuestionRepository.save(question);
    }
    
    @Override
    @Transactional
    public StandardQuestion updateQuestionCategory(Integer questionId, Integer categoryId) {
        StandardQuestion question = standardQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + questionId));
        
        // If categoryId is null, remove the category
        if (categoryId == null) {
            question.setCategory(null);
        } else {
            // Verify the category exists and set it
            QuestionCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
            question.setCategory(category);
        }
        
        question.setUpdatedAt(LocalDateTime.now());
        question.setVersion(question.getVersion() + 1);
        
        return standardQuestionRepository.save(question);
    }
    
    @Override
    @Transactional
    public void deleteStandardQuestion(Integer id) {
        standardQuestionRepository.deleteById(id);
    }

    @Override
    public Page<StandardQuestion> getStandardQuestionsByPage(
            Integer categoryId,
            StandardQuestion.QuestionType questionType,
            StandardQuestion.DifficultyLevel difficulty,
            String keyword,
            Pageable pageable) {
        // 如果关键词为空，则传入null，避免无效的模糊查询
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        
        return standardQuestionRepository.findByFilters(
                categoryId, 
                questionType, 
                difficulty, 
                searchKeyword, 
                pageable);
    }

    @Override
    public Page<StandardQuestion> getQuestionsWithoutCategory(Pageable pageable) {
        return standardQuestionRepository.findByCategoryIsNull(pageable);
    }
    
    @Override
    @Transactional
    public StandardQuestion updateQuestionCategoryByName(Integer questionId, String categoryName) {
        // 查找问题
        StandardQuestion question = standardQuestionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Standard question not found with id: " + questionId));
        
        // 如果分类名称为空，则移除分类
        if (categoryName == null || categoryName.trim().isEmpty()) {
            question.setCategory(null);
            question.setUpdatedAt(LocalDateTime.now());
            question.setVersion(question.getVersion() + 1);
            return standardQuestionRepository.save(question);
        }
        
        // 查找或创建分类
        QuestionCategory category;
        Optional<QuestionCategory> existingCategory = categoryRepository.findByName(categoryName);
        
        if (existingCategory.isPresent()) {
            // 如果分类已存在，使用现有分类
            category = existingCategory.get();
        } else {
            // 如果分类不存在，创建新分类
            QuestionCategory newCategory = new QuestionCategory();
            newCategory.setName(categoryName);
            newCategory.setDescription("自动创建的分类");
            category = categoryRepository.save(newCategory);
        }
        
        // 更新问题的分类
        question.setCategory(category);
        question.setUpdatedAt(LocalDateTime.now());
        question.setVersion(question.getVersion() + 1);
        
        return standardQuestionRepository.save(question);
    }
}