package com.llm.eval.repository;

import com.llm.eval.model.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, Integer> {
    
    Optional<QuestionCategory> findByName(String name);
    
    List<QuestionCategory> findByParentIsNull();
    
    List<QuestionCategory> findByParentCategoryId(Integer parentId);
    
    @Query("SELECT DISTINCT qc FROM QuestionCategory qc LEFT JOIN FETCH qc.children WHERE qc.parent IS NULL")
    List<QuestionCategory> findAllWithChildren();
    
    @Query("SELECT COUNT(qc) FROM QuestionCategory qc")
    long countCategories();
} 