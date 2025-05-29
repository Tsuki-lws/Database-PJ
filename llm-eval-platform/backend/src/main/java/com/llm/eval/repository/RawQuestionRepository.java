package com.llm.eval.repository;

import com.llm.eval.model.RawQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RawQuestionRepository extends JpaRepository<RawQuestion, Integer> {
    
    @Query("SELECT COUNT(rq) FROM RawQuestion rq")
    long countRawQuestions();
    
    @Query("SELECT COUNT(rq) FROM RawQuestion rq WHERE rq.source = :source")
    long countBySource(String source);
} 