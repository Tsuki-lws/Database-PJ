package com.llm.eval.repository;

import com.llm.eval.model.StandardQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StandardQuestionRepository extends JpaRepository<StandardQuestion, Integer> {
    
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq")
    long countStandardQuestions();
    
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq WHERE sq.category.categoryId = :categoryId")
    long countByCategoryId(Integer categoryId);
    
    @Query("SELECT sq FROM StandardQuestion sq JOIN sq.tags t WHERE t.tagId = :tagId")
    List<StandardQuestion> findByTagId(Integer tagId);
    
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq JOIN sq.tags t WHERE t.tagId = :tagId")
    long countByTagId(Integer tagId);
    
    @Query("SELECT sq FROM StandardQuestion sq WHERE sq.standardQuestionId NOT IN " +
           "(SELECT sa.standardQuestion.standardQuestionId FROM StandardAnswer sa WHERE sa.isFinal = true)")
    List<StandardQuestion> findWithoutStandardAnswers();
    
    @Query("SELECT COUNT(sq) FROM StandardQuestion sq WHERE sq.standardQuestionId NOT IN " +
           "(SELECT sa.standardQuestion.standardQuestionId FROM StandardAnswer sa WHERE sa.isFinal = true)")
    long countWithoutStandardAnswers();
    
    List<StandardQuestion> findByCategoryCategoryId(Integer categoryId);
} 