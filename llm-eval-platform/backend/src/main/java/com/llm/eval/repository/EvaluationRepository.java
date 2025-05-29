package com.llm.eval.repository;

import com.llm.eval.model.Evaluation;
import com.llm.eval.model.EvaluationBatch;
import com.llm.eval.model.LlmAnswer;
import com.llm.eval.model.StandardAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Integer> {
    
    List<Evaluation> findByLlmAnswer(LlmAnswer llmAnswer);
    
    List<Evaluation> findByStandardAnswer(StandardAnswer standardAnswer);
    
    List<Evaluation> findByBatch(EvaluationBatch batch);
    
    Optional<Evaluation> findByLlmAnswerAndStandardAnswer(LlmAnswer llmAnswer, StandardAnswer standardAnswer);
    
    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.batch = ?1")
    BigDecimal calculateAverageScoreByBatch(EvaluationBatch batch);
    
    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.batch = ?1 AND e.score >= ?2")
    Long countByBatchAndScoreGreaterThanEqual(EvaluationBatch batch, BigDecimal threshold);
} 