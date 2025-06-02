package com.llm.eval.repository;

import com.llm.eval.model.RawAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RawAnswerRepository extends JpaRepository<RawAnswer, Integer> {
    
    @Query("SELECT COUNT(ra) FROM RawAnswer ra")
    long countRawAnswers();
    
    @Query("SELECT COUNT(ra) FROM RawAnswer ra WHERE ra.question.questionId = :questionId")
    long countByQuestionId(Integer questionId);

    /**
     * 根据源答案ID查询原始答案
     * @param sourceAnswerId 源答案ID
     * @return 原始答案
     */
    Optional<RawAnswer> findBySourceAnswerId(Integer sourceAnswerId);
    
    /**
     * 根据问题ID查询所有回答
     * @param questionId 问题ID
     * @return 回答列表
     */
    List<RawAnswer> findByQuestionQuestionId(Integer questionId);
} 