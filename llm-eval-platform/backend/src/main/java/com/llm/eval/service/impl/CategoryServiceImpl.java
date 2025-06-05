package com.llm.eval.service.impl;

import com.llm.eval.model.QuestionCategory;
import com.llm.eval.repository.QuestionCategoryRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    private final QuestionCategoryRepository categoryRepository;
    private final StandardQuestionRepository questionRepository;
    
    @Autowired
    public CategoryServiceImpl(
            QuestionCategoryRepository categoryRepository,
            StandardQuestionRepository questionRepository) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
    }
    
    @Override
    public List<QuestionCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    @Override
    public List<QuestionCategory> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }
    
    @Override
    public Optional<QuestionCategory> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }
    
    @Override
    public List<QuestionCategory> getChildCategories(Integer parentId) {
        // Verify that the parent category exists
        if (!categoryRepository.existsById(parentId)) {
            throw new EntityNotFoundException("Parent category not found with id: " + parentId);
        }
        
        return categoryRepository.findByParentCategoryId(parentId);
    }
    
    @Override
    @Transactional
    public QuestionCategory createCategory(QuestionCategory category) {
        // If parent ID is provided, verify it exists
        if (category.getParent() != null && category.getParent().getCategoryId() != null) {
            Integer parentId = category.getParent().getCategoryId();
            QuestionCategory parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found with id: " + parentId));
            
            // Set the parent entity
            category.setParent(parent);
        }
        
        return categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    public QuestionCategory updateCategory(Integer id, QuestionCategory category) {
        QuestionCategory existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        // Update basic properties
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        
        // Update parent if provided and different
        if (category.getParent() != null && category.getParent().getCategoryId() != null) {
            Integer newParentId = category.getParent().getCategoryId();
            
            // Avoid circular references
            if (newParentId.equals(id)) {
                throw new IllegalArgumentException("A category cannot be its own parent");
            }
            
            QuestionCategory newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found with id: " + newParentId));
            
            existingCategory.setParent(newParent);
        } else if (category.getParent() == null) {
            // Remove parent if set to null
            existingCategory.setParent(null);
        }
        
        return categoryRepository.save(existingCategory);
    }
    
    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }
    
    @Override
    public List<QuestionCategory> getCategoriesWithQuestionCount() {
        List<QuestionCategory> categories = categoryRepository.findAll();
        
        // 为每个分类添加问题数量信息
        for (QuestionCategory category : categories) {
            // 获取该分类下的标准问题数量
            long count = questionRepository.countByCategoryId(category.getCategoryId());
            // 将问题数量作为临时属性存储在category对象中
            category.setQuestionCount(count);
        }
        
        return categories;
    }
} 