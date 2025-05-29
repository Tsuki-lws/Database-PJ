package com.llm.eval.repository;

import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.StandardQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandardAnswerRepository extends JpaRepository<StandardAnswer, Integer> {
    
    @Query("SELECT COUNT(sa) FROM StandardAnswer sa")
    long countStandardAnswers();
    
    @Query("SELECT COUNT(sa) FROM StandardAnswer sa WHERE sa.isFinal = true")
    long countFinalStandardAnswers();
    
    List<StandardAnswer> findByStandardQuestionStandardQuestionId(Integer standardQuestionId);
    
    Optional<StandardAnswer> findByStandardQuestionStandardQuestionIdAndIsFinalTrue(Integer standardQuestionId);
    
    Optional<StandardAnswer> findByStandardQuestionAndIsFinalTrue(StandardQuestion standardQuestion);
} 