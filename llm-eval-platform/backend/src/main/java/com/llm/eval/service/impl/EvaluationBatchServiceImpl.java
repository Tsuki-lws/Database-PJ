package com.llm.eval.service.impl;

import com.llm.eval.dto.DatasetVersionDTO;
import com.llm.eval.dto.EvaluationBatchDTO;
import com.llm.eval.dto.LlmModelDTO;
import com.llm.eval.model.EvaluationBatch;
import com.llm.eval.model.EvaluationBatch.EvaluationStatus;
import com.llm.eval.model.LlmModel;
import com.llm.eval.model.DatasetVersion;
import com.llm.eval.repository.EvaluationBatchRepository;
import com.llm.eval.repository.LlmModelRepository;
import com.llm.eval.repository.DatasetVersionRepository;
import com.llm.eval.service.EvaluationBatchService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationBatchServiceImpl implements EvaluationBatchService {

    private final EvaluationBatchRepository batchRepository;
    private final LlmModelRepository modelRepository;
    private final DatasetVersionRepository versionRepository;

    @Autowired
    public EvaluationBatchServiceImpl(
            EvaluationBatchRepository batchRepository,
            LlmModelRepository modelRepository,
            DatasetVersionRepository versionRepository) {
        this.batchRepository = batchRepository;
        this.modelRepository = modelRepository;
        this.versionRepository = versionRepository;
    }

    @Override
    public List<EvaluationBatchDTO> getAllBatches() {
        return batchRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EvaluationBatchDTO> getBatchById(Integer batchId) {
        return batchRepository.findById(batchId)
                .map(this::convertToDTO);
    }

    @Override
    public List<EvaluationBatchDTO> getBatchesByModelId(Integer modelId) {
        Optional<LlmModel> model = modelRepository.findById(modelId);
        if (model.isEmpty()) {
            return List.of();
        }
        
        return batchRepository.findByModel(model.get()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EvaluationBatchDTO> getBatchesByDatasetVersionId(Integer versionId) {
        Optional<DatasetVersion> version = versionRepository.findById(versionId);
        if (version.isEmpty()) {
            return List.of();
        }
        
        return batchRepository.findByDatasetVersion(version.get()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EvaluationBatchDTO> getBatchesByStatus(EvaluationStatus status) {
        return batchRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EvaluationBatchDTO createBatch(EvaluationBatchDTO batchDTO) {
        EvaluationBatch batch = convertToEntity(batchDTO);
        
        // 设置默认值
        batch.setStatus(EvaluationStatus.PENDING);
        batch.setCreatedAt(LocalDateTime.now());
        
        EvaluationBatch savedBatch = batchRepository.save(batch);
        return convertToDTO(savedBatch);
    }

    @Override
    @Transactional
    public Optional<EvaluationBatchDTO> updateBatch(Integer batchId, EvaluationBatchDTO batchDTO) {
        if (!batchRepository.existsById(batchId)) {
            return Optional.empty();
        }
        
        EvaluationBatch batch = convertToEntity(batchDTO);
        batch.setBatchId(batchId);
        
        EvaluationBatch updatedBatch = batchRepository.save(batch);
        return Optional.of(convertToDTO(updatedBatch));
    }

    @Override
    @Transactional
    public boolean deleteBatch(Integer batchId) {
        if (!batchRepository.existsById(batchId)) {
            return false;
        }
        
        batchRepository.deleteById(batchId);
        return true;
    }

    @Override
    @Transactional
    public boolean startBatch(Integer batchId) {
        Optional<EvaluationBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            return false;
        }
        
        EvaluationBatch batch = batchOpt.get();
        batch.setStatus(EvaluationStatus.IN_PROGRESS);
        batch.setStartTime(LocalDateTime.now());
        
        batchRepository.save(batch);
        return true;
    }

    @Override
    @Transactional
    public boolean completeBatch(Integer batchId) {
        Optional<EvaluationBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            return false;
        }
        
        EvaluationBatch batch = batchOpt.get();
        batch.setStatus(EvaluationStatus.COMPLETED);
        batch.setEndTime(LocalDateTime.now());
        
        batchRepository.save(batch);
        return true;
    }

    @Override
    @Transactional
    public Optional<EvaluationBatchDTO> updateBatchMetrics(Integer batchId) {
        Optional<EvaluationBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            return Optional.empty();
        }
        
        // 这里可以添加计算评估指标的逻辑
        // 例如：平均分数、通过率等
        
        return batchOpt.map(this::convertToDTO);
    }
    
    // 辅助方法：将实体转换为DTO
    private EvaluationBatchDTO convertToDTO(EvaluationBatch batch) {
        EvaluationBatchDTO dto = new EvaluationBatchDTO();
        BeanUtils.copyProperties(batch, dto);
        
        if (batch.getModel() != null) {
            LlmModelDTO modelDTO = new LlmModelDTO();
            BeanUtils.copyProperties(batch.getModel(), modelDTO);
            dto.setModel(modelDTO);
        }
        
        if (batch.getDatasetVersion() != null) {
            DatasetVersionDTO versionDTO = new DatasetVersionDTO();
            BeanUtils.copyProperties(batch.getDatasetVersion(), versionDTO);
            dto.setDatasetVersion(versionDTO);
        }
        
        if (batch.getJudgeModel() != null) {
            LlmModelDTO judgeModelDTO = new LlmModelDTO();
            BeanUtils.copyProperties(batch.getJudgeModel(), judgeModelDTO);
            dto.setJudgeModel(judgeModelDTO);
        }
        
        return dto;
    }
    
    // 辅助方法：将DTO转换为实体
    private EvaluationBatch convertToEntity(EvaluationBatchDTO dto) {
        EvaluationBatch entity = new EvaluationBatch();
        BeanUtils.copyProperties(dto, entity);
        
        if (dto.getModel() != null && dto.getModel().getModelId() != null) {
            modelRepository.findById(dto.getModel().getModelId())
                    .ifPresent(entity::setModel);
        }
        
        if (dto.getDatasetVersion() != null && dto.getDatasetVersion().getVersionId() != null) {
            versionRepository.findById(dto.getDatasetVersion().getVersionId())
                    .ifPresent(entity::setDatasetVersion);
        }
        
        if (dto.getJudgeModel() != null && dto.getJudgeModel().getModelId() != null) {
            modelRepository.findById(dto.getJudgeModel().getModelId())
                    .ifPresent(entity::setJudgeModel);
        }
        
        return entity;
    }
} 