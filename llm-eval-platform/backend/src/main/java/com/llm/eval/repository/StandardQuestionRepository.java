package com.llm.eval.repository;

import com.llm.eval.model.StandardQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    
    @Query("SELECT sq FROM StandardQuestion sq JOIN sq.tags t WHERE t.tagName = :tagName")
    List<StandardQuestion> findByTagName(String tagName);
    
    /**
     * 根据问题文本和源问题ID查询标准问题
     * @param question 问题文本
     * @param sourceQuestionId 源问题ID
     * @return 标准问题
     */
    @Query("SELECT sq FROM StandardQuestion sq WHERE sq.question = :question AND " +
           "((:sourceQuestionId IS NULL AND sq.sourceQuestion IS NULL) OR " +
           "(sq.sourceQuestion.questionId = :sourceQuestionId))")
    Optional<StandardQuestion> findByQuestionAndSourceQuestionId(String question, Integer sourceQuestionId);
} 