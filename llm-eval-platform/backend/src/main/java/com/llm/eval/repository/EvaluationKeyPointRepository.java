package com.llm.eval.repository;

import com.llm.eval.model.AnswerKeyPoint;
import com.llm.eval.model.Evaluation;
import com.llm.eval.model.EvaluationKeyPoint;
import com.llm.eval.model.EvaluationKeyPoint.KeyPointStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationKeyPointRepository extends JpaRepository<EvaluationKeyPoint, Integer> {
    
    List<EvaluationKeyPoint> findByEvaluation(Evaluation evaluation);
    
    List<EvaluationKeyPoint> findByKeyPoint(AnswerKeyPoint keyPoint);
    
    List<EvaluationKeyPoint> findByEvaluationAndStatus(Evaluation evaluation, KeyPointStatus status);
    
    long countByEvaluationAndStatus(Evaluation evaluation, KeyPointStatus status);
} 