package com.llm.eval.repository;

import com.llm.eval.model.AnswerKeyPoint;
import com.llm.eval.model.StandardAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerKeyPointRepository extends JpaRepository<AnswerKeyPoint, Integer> {
    
    List<AnswerKeyPoint> findByStandardAnswer(StandardAnswer standardAnswer);
    
    List<AnswerKeyPoint> findByStandardAnswerOrderByPointOrder(StandardAnswer standardAnswer);
    
    void deleteByStandardAnswer(StandardAnswer standardAnswer);
} 