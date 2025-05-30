package com.llm.eval.service;

import com.llm.eval.dto.EvaluationBatchDTO;
import com.llm.eval.model.EvaluationBatch.EvaluationStatus;

import java.util.List;
import java.util.Optional;

public interface EvaluationBatchService {
    
    List<EvaluationBatchDTO> getAllBatches();
    
    Optional<EvaluationBatchDTO> getBatchById(Integer batchId);
    
    List<EvaluationBatchDTO> getBatchesByModelId(Integer modelId);
    
    List<EvaluationBatchDTO> getBatchesByDatasetVersionId(Integer versionId);
    
    List<EvaluationBatchDTO> getBatchesByStatus(EvaluationStatus status);
    
    EvaluationBatchDTO createBatch(EvaluationBatchDTO batchDTO);
    
    Optional<EvaluationBatchDTO> updateBatch(Integer batchId, EvaluationBatchDTO batchDTO);
    
    boolean deleteBatch(Integer batchId);
    
    boolean startBatch(Integer batchId);
    
    boolean completeBatch(Integer batchId);
    
    Optional<EvaluationBatchDTO> updateBatchMetrics(Integer batchId);
} 