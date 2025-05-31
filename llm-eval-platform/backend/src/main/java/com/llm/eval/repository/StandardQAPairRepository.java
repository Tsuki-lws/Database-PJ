package com.llm.eval.repository;

import com.llm.eval.model.StandardQAPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StandardQAPairRepository extends JpaRepository<StandardQAPair, Integer> {
    
    /**
     * 根据标准问题ID和标准答案ID查找问答对关联
     */
    Optional<StandardQAPair> findByStandardQuestion_StandardQuestionIdAndStandardAnswer_StandardAnswerId(Integer standardQuestionId, Integer standardAnswerId);
} 