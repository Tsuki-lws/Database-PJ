package com.llm.eval.service.impl;

import com.llm.eval.dto.LlmAnswerDTO;
import com.llm.eval.model.LlmAnswer;
import com.llm.eval.model.LlmModel;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.EvaluationBatch;
import com.llm.eval.model.DatasetVersion;
import com.llm.eval.repository.LlmAnswerRepository;
import com.llm.eval.repository.LlmModelRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.EvaluationBatchRepository;
import com.llm.eval.repository.DatasetVersionRepository;
import com.llm.eval.service.LlmAnswerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LlmAnswerServiceImpl implements LlmAnswerService {

    private final LlmAnswerRepository llmAnswerRepository;
    private final LlmModelRepository llmModelRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    private final EvaluationBatchRepository batchRepository;
    private final DatasetVersionRepository datasetVersionRepository;

    @Autowired
    public LlmAnswerServiceImpl(
            LlmAnswerRepository llmAnswerRepository,
            LlmModelRepository llmModelRepository,
            StandardQuestionRepository standardQuestionRepository,
            EvaluationBatchRepository batchRepository,
            DatasetVersionRepository datasetVersionRepository) {
        this.llmAnswerRepository = llmAnswerRepository;
        this.llmModelRepository = llmModelRepository;
        this.standardQuestionRepository = standardQuestionRepository;
        this.batchRepository = batchRepository;
        this.datasetVersionRepository = datasetVersionRepository;
    }

    @Override
    public List<LlmAnswerDTO> getAllAnswers() {
        return llmAnswerRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LlmAnswerDTO> getAnswerById(Integer answerId) {
        return llmAnswerRepository.findById(answerId)
                .map(this::convertToDTO);
    }

    @Override
    public List<LlmAnswerDTO> getAnswersByModelId(Integer modelId) {
        Optional<LlmModel> model = llmModelRepository.findById(modelId);
        if (model.isEmpty()) {
            return new ArrayList<>();
        }
        return llmAnswerRepository.findByModel(model.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LlmAnswerDTO> getAnswersByQuestionId(Integer questionId) {
        Optional<StandardQuestion> question = standardQuestionRepository.findById(questionId);
        if (question.isEmpty()) {
            return new ArrayList<>();
        }
        return llmAnswerRepository.findByStandardQuestion(question.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LlmAnswerDTO> getAnswersByBatchId(Integer batchId) {
        Optional<EvaluationBatch> batch = batchRepository.findById(batchId);
        if (batch.isEmpty()) {
            return new ArrayList<>();
        }
        return llmAnswerRepository.findByBatch(batch.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LlmAnswerDTO createAnswer(LlmAnswerDTO answerDTO) {
        LlmAnswer answer = convertToEntity(answerDTO);
        answer = llmAnswerRepository.save(answer);
        return convertToDTO(answer);
    }

    @Override
    @Transactional
    public Optional<LlmAnswerDTO> updateAnswer(Integer answerId, LlmAnswerDTO answerDTO) {
        if (!llmAnswerRepository.existsById(answerId)) {
            return Optional.empty();
        }
        
        LlmAnswer answer = convertToEntity(answerDTO);
        answer.setLlmAnswerId(answerId);
        answer = llmAnswerRepository.save(answer);
        
        return Optional.of(convertToDTO(answer));
    }

    @Override
    @Transactional
    public boolean deleteAnswer(Integer answerId) {
        if (!llmAnswerRepository.existsById(answerId)) {
            return false;
        }
        
        llmAnswerRepository.deleteById(answerId);
        return true;
    }

    @Override
    @Transactional
    public List<LlmAnswerDTO> generateAnswersForBatch(Integer batchId) {
        // 这里应该实现批量生成回答的逻辑
        // 由于这是一个复杂的功能，这里只返回空列表作为占位符
        return new ArrayList<>();
    }
    
    private LlmAnswerDTO convertToDTO(LlmAnswer entity) {
        if (entity == null) {
            return null;
        }
        
        LlmAnswerDTO dto = new LlmAnswerDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 手动处理复杂属性
        if (entity.getModel() != null) {
            dto.setModel(convertModelToDTO(entity.getModel()));
        }
        
        if (entity.getStandardQuestion() != null) {
            dto.setStandardQuestion(convertQuestionToDTO(entity.getStandardQuestion()));
        }
        
        if (entity.getBatch() != null) {
            dto.setBatchId(entity.getBatch().getBatchId());
        }
        
        if (entity.getParentAnswer() != null) {
            dto.setParentAnswerId(entity.getParentAnswer().getLlmAnswerId());
        }
        
        return dto;
    }
    
    private LlmAnswer convertToEntity(LlmAnswerDTO dto) {
        if (dto == null) {
            return null;
        }
        
        LlmAnswer entity = new LlmAnswer();
        
        // 复制基本属性
        entity.setLlmAnswerId(dto.getLlmAnswerId());
        entity.setContent(dto.getContent());
        entity.setLatency(dto.getLatency());
        entity.setTokensUsed(dto.getTokensUsed());
        entity.setPromptTemplate(dto.getPromptTemplate());
        entity.setPromptParams(dto.getPromptParams());
        entity.setTemperature(dto.getTemperature());
        entity.setTopP(dto.getTopP());
        entity.setRetryCount(dto.getRetryCount());
        entity.setIsFinal(dto.getIsFinal());
        entity.setCreatedAt(dto.getCreatedAt());
        
        // 处理关联实体
        if (dto.getModel() != null && dto.getModel().getModelId() != null) {
            llmModelRepository.findById(dto.getModel().getModelId())
                    .ifPresent(entity::setModel);
        }
        
        if (dto.getStandardQuestion() != null && dto.getStandardQuestion().getStandardQuestionId() != null) {
            standardQuestionRepository.findById(dto.getStandardQuestion().getStandardQuestionId())
                    .ifPresent(entity::setStandardQuestion);
        }
        
        // 设置一个默认的数据集版本（如果需要）
        // 尝试获取最新的数据集版本，如果没有则创建一个临时的
        datasetVersionRepository.findAll().stream()
                .findFirst()
                .ifPresentOrElse(
                        entity::setDatasetVersion,
                        () -> {
                            DatasetVersion defaultVersion = new DatasetVersion();
                            defaultVersion.setVersionId(1);
                            defaultVersion.setName("默认版本");
                            defaultVersion.setIsPublished(true);
                            entity.setDatasetVersion(defaultVersion);
                        });
        
        if (dto.getBatchId() != null) {
            batchRepository.findById(dto.getBatchId())
                    .ifPresent(entity::setBatch);
        }
        
        if (dto.getParentAnswerId() != null) {
            llmAnswerRepository.findById(dto.getParentAnswerId())
                    .ifPresent(entity::setParentAnswer);
        }
        
        return entity;
    }
    
    // 辅助方法，转换LlmModel到DTO
    private com.llm.eval.dto.LlmModelDTO convertModelToDTO(LlmModel model) {
        com.llm.eval.dto.LlmModelDTO modelDTO = new com.llm.eval.dto.LlmModelDTO();
        modelDTO.setModelId(model.getModelId());
        modelDTO.setName(model.getName());
        modelDTO.setVersion(model.getVersion());
        modelDTO.setProvider(model.getProvider());
        modelDTO.setDescription(model.getDescription());
        modelDTO.setApiConfig(model.getApiConfig());
        modelDTO.setCreatedAt(model.getCreatedAt());
        return modelDTO;
    }
    
    // 辅助方法，转换StandardQuestion到DTO
    private com.llm.eval.dto.StandardQuestionDTO convertQuestionToDTO(StandardQuestion question) {
        com.llm.eval.dto.StandardQuestionDTO questionDTO = new com.llm.eval.dto.StandardQuestionDTO();
        questionDTO.setStandardQuestionId(question.getStandardQuestionId());
        questionDTO.setQuestion(question.getQuestion());
        questionDTO.setQuestionType(question.getQuestionType() != null ? question.getQuestionType().toString() : null);
        questionDTO.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().toString() : null);
        return questionDTO;
    }
} 