package com.llm.eval.repository;

import com.llm.eval.model.CrowdsourcedAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrowdsourcedAnswerRepository extends JpaRepository<CrowdsourcedAnswer, Integer> {
    
    @Query("SELECT COUNT(ca) FROM CrowdsourcedAnswer ca")
    long countCrowdsourcedAnswers();
    
    @Query("SELECT COUNT(ca) FROM CrowdsourcedAnswer ca WHERE ca.standardQuestion.standardQuestionId = :questionId")
    long countByQuestionId(Integer questionId);
    
    @Query("SELECT COUNT(ca) FROM CrowdsourcedAnswer ca WHERE ca.reviewStatus = :status")
    long countByReviewStatus(CrowdsourcedAnswer.ReviewStatus status);
    
    List<CrowdsourcedAnswer> findByStandardQuestionStandardQuestionId(Integer questionId);
    
    List<CrowdsourcedAnswer> findByStandardQuestionStandardQuestionIdAndReviewStatus(
            Integer questionId, CrowdsourcedAnswer.ReviewStatus status);
} 