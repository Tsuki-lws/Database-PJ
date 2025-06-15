package com.llm.eval.repository;

import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.EvaluationBatch;
import com.llm.eval.model.LlmAnswer;
import com.llm.eval.model.LlmModel;
import com.llm.eval.model.StandardQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LlmAnswerRepository extends JpaRepository<LlmAnswer, Integer> {
    
    List<LlmAnswer> findByModel(LlmModel model);
    
    List<LlmAnswer> findByStandardQuestion(StandardQuestion standardQuestion);
    
    List<LlmAnswer> findByBatch(EvaluationBatch batch);
    
    List<LlmAnswer> findByBatchAndIsFinalTrue(EvaluationBatch batch);
    
    List<LlmAnswer> findByModelAndBatch(LlmModel model, EvaluationBatch batch);
    
    List<LlmAnswer> findByStandardQuestionAndBatch(StandardQuestion standardQuestion, EvaluationBatch batch);
    
    List<LlmAnswer> findByDatasetVersion(DatasetVersion datasetVersion);
    
    long countByDatasetVersion(DatasetVersion datasetVersion);
    
    List<LlmAnswer> findByStandardQuestionAndDatasetVersion(StandardQuestion standardQuestion, DatasetVersion datasetVersion);
} 