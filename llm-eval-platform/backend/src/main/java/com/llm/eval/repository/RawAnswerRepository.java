package com.llm.eval.repository;

import com.llm.eval.model.RawAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RawAnswerRepository extends JpaRepository<RawAnswer, Integer> {
    
    @Query("SELECT COUNT(ra) FROM RawAnswer ra")
    long countRawAnswers();
    
    @Query("SELECT COUNT(ra) FROM RawAnswer ra WHERE ra.question.questionId = :questionId")
    long countByQuestionId(Integer questionId);
} 