package com.llm.eval.repository;

import com.llm.eval.model.EvaluationBatch;
import com.llm.eval.model.EvaluationBatch.EvaluationStatus;
import com.llm.eval.model.LlmModel;
import com.llm.eval.model.DatasetVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationBatchRepository extends JpaRepository<EvaluationBatch, Integer> {
    
    List<EvaluationBatch> findByModel(LlmModel model);
    
    List<EvaluationBatch> findByDatasetVersion(DatasetVersion datasetVersion);
    
    List<EvaluationBatch> findByStatus(EvaluationStatus status);
    
    List<EvaluationBatch> findByModelAndDatasetVersion(LlmModel model, DatasetVersion datasetVersion);
} 