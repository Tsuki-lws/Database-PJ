/*
package com.llm.eval.service.impl;

import com.llm.eval.dto.EvaluationBatchDTO;
import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.EvaluationBatch;
import com.llm.eval.model.EvaluationBatch.EvaluationStatus;
import com.llm.eval.model.LlmModel;
import com.llm.eval.repository.DatasetVersionRepository;
import com.llm.eval.repository.EvaluationBatchRepository;
import com.llm.eval.repository.EvaluationRepository;
import com.llm.eval.repository.LlmModelRepository;
import com.llm.eval.service.EvaluationBatchService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationBatchServiceImpl implements EvaluationBatchService {

    private final EvaluationBatchRepository batchRepository;
    private final LlmModelRepository modelRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final EvaluationRepository evaluationRepository;

    @Autowired
    public EvaluationBatchServiceImpl(
            EvaluationBatchRepository batchRepository,
            LlmModelRepository modelRepository,
            DatasetVersionRepository datasetVersionRepository,
            EvaluationRepository evaluationRepository) {
        this.batchRepository = batchRepository;
        this.modelRepository = modelRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    public List<EvaluationBatchDTO> getAllBatches() {
        return batchRepository.findAll()
                .stream()
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
        return batchRepository.findByModel(model.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EvaluationBatchDTO> getBatchesByDatasetVersionId(Integer versionId) {
        Optional<DatasetVersion> version = datasetVersionRepository.findById(versionId);
        if (version.isEmpty()) {
            return List.of();
        }
        return batchRepository.findByDatasetVersion(version.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EvaluationBatchDTO> getBatchesByStatus(EvaluationStatus status) {
        return batchRepository.findByStatus(status)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EvaluationBatchDTO createBatch(EvaluationBatchDTO batchDTO) {
        EvaluationBatch batch = convertToEntity(batchDTO);
        
        // 设置默认状态
        batch.setStatus(EvaluationStatus.PENDING);
        batch.setCreatedAt(LocalDateTime.now());
        batch.setUpdatedAt(LocalDateTime.now());
        
        batch = batchRepository.save(batch);
        return convertToDTO(batch);
    }

    @Override
    @Transactional
    public Optional<EvaluationBatchDTO> updateBatch(Integer batchId, EvaluationBatchDTO batchDTO) {
        Optional<EvaluationBatch> existingBatchOpt = batchRepository.findById(batchId);
        if (existingBatchOpt.isEmpty()) {
            return Optional.empty();
        }
        
        EvaluationBatch existingBatch = existingBatchOpt.get();
        
        // 只能更新待处理状态的批次
        if (existingBatch.getStatus() != EvaluationStatus.PENDING) {
            return Optional.of(convertToDTO(existingBatch));
        }
        
        // 更新可以修改的字段
        existingBatch.setName(batchDTO.getName());
        existingBatch.setDescription(batchDTO.getDescription());
        
        // 更新关联实体
        if (batchDTO.getModel() != null && batchDTO.getModel().getModelId() != null) {
            Optional<LlmModel> model = modelRepository.findById(batchDTO.getModel().getModelId());
            model.ifPresent(existingBatch::setModel);
        }
        
        if (batchDTO.getJudgeModel() != null && batchDTO.getJudgeModel().getModelId() != null) {
            Optional<LlmModel> judgeModel = modelRepository.findById(batchDTO.getJudgeModel().getModelId());
            judgeModel.ifPresent(existingBatch::setJudgeModel);
        }
        
        if (batchDTO.getDatasetVersion() != null && batchDTO.getDatasetVersion().getVersionId() != null) {
            Optional<DatasetVersion> version = datasetVersionRepository.findById(batchDTO.getDatasetVersion().getVersionId());
            version.ifPresent(existingBatch::setDatasetVersion);
        }
        
        if (batchDTO.getEvaluationMethod() != null) {
            existingBatch.setEvaluationMethod(batchDTO.getEvaluationMethod());
        }
        
        existingBatch.setUpdatedAt(LocalDateTime.now());
        existingBatch = batchRepository.save(existingBatch);
        
        return Optional.of(convertToDTO(existingBatch));
    }

    @Override
    @Transactional
    public boolean deleteBatch(Integer batchId) {
        Optional<EvaluationBatch> batch = batchRepository.findById(batchId);
        if (batch.isEmpty()) {
            return false;
        }
        
        // 只能删除待处理状态的批次
        if (batch.get().getStatus() != EvaluationStatus.PENDING) {
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
        
        // 只能启动待处理状态的批次
        if (batch.getStatus() != EvaluationStatus.PENDING) {
            return false;
        }
        
        batch.setStatus(EvaluationStatus.IN_PROGRESS);
        batch.setStartTime(LocalDateTime.now());
        batch.setUpdatedAt(LocalDateTime.now());
        
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
        
        // 只能完成进行中状态的批次
        if (batch.getStatus() != EvaluationStatus.IN_PROGRESS) {
            return false;
        }
        
        batch.setStatus(EvaluationStatus.COMPLETED);
        batch.setEndTime(LocalDateTime.now());
        batch.setUpdatedAt(LocalDateTime.now());
        
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
        
        EvaluationBatch batch = batchOpt.get();
        
        // 计算评测指标
        BigDecimal averageScore = evaluationRepository.calculateAverageScoreByBatch(batch);
        
        // 更新批次指标摘要
        StringBuilder metricsSummary = new StringBuilder();
        metricsSummary.append("{\"averageScore\": ").append(averageScore)
                .append(", \"totalQuestions\": ").append(batch.getDatasetVersion().getQuestionCount())
                .append(", \"completedAt\": \"").append(LocalDateTime.now()).append("\"")
                .append("}");
        
        batch.setMetricsSummary(metricsSummary.toString());
        batch.setUpdatedAt(LocalDateTime.now());
        
        batch = batchRepository.save(batch);
        return Optional.of(convertToDTO(batch));
    }

    // 转换方法
    private EvaluationBatchDTO convertToDTO(EvaluationBatch batch) {
        EvaluationBatchDTO dto = new EvaluationBatchDTO();
        BeanUtils.copyProperties(batch, dto);
        
        // 设置统计数据
        if (batch.getMetricsSummary() != null && !batch.getMetricsSummary().isEmpty()) {
            // 这里可以解析JSON字符串获取统计数据
            // 简化实现，实际项目中应该使用JSON解析库
            dto.setTotalQuestions(batch.getDatasetVersion() != null ? batch.getDatasetVersion().getQuestionCount() : 0);
            
            // 从评测数据中获取已完成问题数量
            Long completedCount = evaluationRepository.countByBatchAndScoreGreaterThanEqual(batch, BigDecimal.ZERO);
            dto.setCompletedQuestions(completedCount.intValue());
            
            // 从评测数据中获取平均分
            BigDecimal averageScore = evaluationRepository.calculateAverageScoreByBatch(batch);
            dto.setAverageScore(averageScore != null ? averageScore.doubleValue() : 0.0);
        }
        
        return dto;
    }

    private EvaluationBatch convertToEntity(EvaluationBatchDTO dto) {
        EvaluationBatch entity = new EvaluationBatch();
        BeanUtils.copyProperties(dto, entity, "model", "judgeModel", "datasetVersion");
        
        // 设置关联实体
        if (dto.getModel() != null && dto.getModel().getModelId() != null) {
            modelRepository.findById(dto.getModel().getModelId())
                    .ifPresent(entity::setModel);
        }
        
        if (dto.getJudgeModel() != null && dto.getJudgeModel().getModelId() != null) {
            modelRepository.findById(dto.getJudgeModel().getModelId())
                    .ifPresent(entity::setJudgeModel);
        }
        
        if (dto.getDatasetVersion() != null && dto.getDatasetVersion().getVersionId() != null) {
            datasetVersionRepository.findById(dto.getDatasetVersion().getVersionId())
                    .ifPresent(entity::setDatasetVersion);
        }
        
        return entity;
    }
}
*/ 