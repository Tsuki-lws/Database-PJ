package com.llm.eval.repository;

import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.StandardQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandardAnswerRepository extends JpaRepository<StandardAnswer, Integer> {
    
    @Query("SELECT COUNT(sa) FROM StandardAnswer sa")
    long countStandardAnswers();
    
    @Query("SELECT COUNT(sa) FROM StandardAnswer sa WHERE sa.isFinal = true")
    long countFinalStandardAnswers();
    
    @Query("SELECT COUNT(sa) FROM StandardAnswer sa WHERE " +
           "(:questionId IS NULL OR sa.standardQuestion.standardQuestionId = :questionId) AND " +
           "(:isFinal IS NULL OR sa.isFinal = :isFinal)")
    long countByFilters(@Param("questionId") Integer questionId, @Param("isFinal") Boolean isFinal);
    
    @Query("SELECT sa FROM StandardAnswer sa WHERE " +
           "(:questionId IS NULL OR sa.standardQuestion.standardQuestionId = :questionId) AND " +
           "(:isFinal IS NULL OR sa.isFinal = :isFinal)")
    Page<StandardAnswer> findByFilters(
            @Param("questionId") Integer questionId, 
            @Param("isFinal") Boolean isFinal, 
            Pageable pageable);
    
    List<StandardAnswer> findByStandardQuestionStandardQuestionId(Integer standardQuestionId);
    
    Optional<StandardAnswer> findByStandardQuestionStandardQuestionIdAndIsFinalTrue(Integer standardQuestionId);
    
    Optional<StandardAnswer> findByStandardQuestionAndIsFinalTrue(StandardQuestion standardQuestion);
    
    // 通过标准问题ID查找标准答案
    List<StandardAnswer> findByStandardQuestionId(Integer standardQuestionId);
    
    // 通过标准问题ID和isFinal标志查找标准答案
    Optional<StandardAnswer> findByStandardQuestionIdAndIsFinalTrue(Integer standardQuestionId);
} 