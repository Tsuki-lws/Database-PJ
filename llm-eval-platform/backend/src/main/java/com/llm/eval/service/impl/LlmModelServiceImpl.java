package com.llm.eval.service.impl;

import com.llm.eval.dto.LlmModelDTO;
import com.llm.eval.model.LlmModel;
import com.llm.eval.repository.LlmModelRepository;
import com.llm.eval.service.LlmModelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LlmModelServiceImpl implements LlmModelService {

    private final LlmModelRepository llmModelRepository;

    @Autowired
    public LlmModelServiceImpl(LlmModelRepository llmModelRepository) {
        this.llmModelRepository = llmModelRepository;
    }

    @Override
    public List<LlmModelDTO> getAllModels() {
        return llmModelRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LlmModelDTO> getModelById(Integer modelId) {
        return llmModelRepository.findById(modelId)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public LlmModelDTO createModel(LlmModelDTO modelDTO) {
        LlmModel model = convertToEntity(modelDTO);
        model = llmModelRepository.save(model);
        return convertToDTO(model);
    }

    @Override
    @Transactional
    public Optional<LlmModelDTO> updateModel(Integer modelId, LlmModelDTO modelDTO) {
        if (!llmModelRepository.existsById(modelId)) {
            return Optional.empty();
        }
        
        LlmModel model = convertToEntity(modelDTO);
        model.setModelId(modelId);
        model = llmModelRepository.save(model);
        
        return Optional.of(convertToDTO(model));
    }

    @Override
    @Transactional
    public boolean deleteModel(Integer modelId) {
        if (!llmModelRepository.existsById(modelId)) {
            return false;
        }
        
        llmModelRepository.deleteById(modelId);
        return true;
    }

    @Override
    public boolean existsByNameAndVersion(String name, String version) {
        return llmModelRepository.existsByNameAndVersion(name, version);
    }
    
    private LlmModelDTO convertToDTO(LlmModel model) {
        LlmModelDTO dto = new LlmModelDTO();
        BeanUtils.copyProperties(model, dto);
        return dto;
    }
    
    private LlmModel convertToEntity(LlmModelDTO dto) {
        LlmModel entity = new LlmModel();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
} 