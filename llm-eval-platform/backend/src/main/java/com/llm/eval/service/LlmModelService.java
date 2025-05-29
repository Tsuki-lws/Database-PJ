package com.llm.eval.service;

import com.llm.eval.dto.LlmModelDTO;

import java.util.List;
import java.util.Optional;

public interface LlmModelService {
    
    List<LlmModelDTO> getAllModels();
    
    Optional<LlmModelDTO> getModelById(Integer modelId);
    
    LlmModelDTO createModel(LlmModelDTO modelDTO);
    
    Optional<LlmModelDTO> updateModel(Integer modelId, LlmModelDTO modelDTO);
    
    boolean deleteModel(Integer modelId);
    
    boolean existsByNameAndVersion(String name, String version);
} 