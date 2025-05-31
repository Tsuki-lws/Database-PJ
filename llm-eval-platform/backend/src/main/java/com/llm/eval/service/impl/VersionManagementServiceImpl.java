package com.llm.eval.service.impl;

import com.llm.eval.dto.*;
import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.StandardQuestionVersion;
import com.llm.eval.model.QuestionCategory;
import com.llm.eval.repository.DatasetVersionRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.StandardQuestionVersionRepository;
import com.llm.eval.service.VersionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class VersionManagementServiceImpl implements VersionManagementService {
    
    @Autowired
    private DatasetVersionRepository datasetVersionRepository;
    
    @Autowired
    private StandardQuestionRepository standardQuestionRepository;
    
    @Autowired
    private StandardQuestionVersionRepository questionVersionRepository;
    
    @Override
    public DatasetVersionDTO createDatasetVersion(CreateDatasetVersionRequest request) {
        // 检查版本名称是否已存在
        if (datasetVersionRepository.existsByName(request.getVersionName())) {
            throw new RuntimeException("版本名称已存在: " + request.getVersionName());
        }
        
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setName(request.getVersionName());
        datasetVersion.setDescription(request.getDescription());
        datasetVersion.setQuestionCount(request.getQuestionIds() != null ? request.getQuestionIds().size() : 0);
        datasetVersion.setIsPublished(false);
        
        // 如果基于现有版本创建，复制问题列表
        if (request.getBaseVersionId() != null) {
            Optional<DatasetVersion> baseVersion = datasetVersionRepository.findById(request.getBaseVersionId());
            if (baseVersion.isPresent() && request.getQuestionIds() == null) {
                // 如果没有指定问题列表，则继承基础版本的问题
                datasetVersion.setQuestions(baseVersion.get().getQuestions());
                datasetVersion.setQuestionCount(baseVersion.get().getQuestionCount());
            }
        }
        
        datasetVersion = datasetVersionRepository.save(datasetVersion);
        
        return convertToDatasetVersionDTO(datasetVersion, true);
    }
    
    @Override
    public PagedResponseDTO<DatasetVersionDTO> getDatasetVersions(Pageable pageable) {
        Page<DatasetVersion> versionsPage = datasetVersionRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        // 获取最新版本以标记isLatest
        Optional<DatasetVersion> latestVersion = datasetVersionRepository.findLatestVersion();
        Integer latestVersionId = latestVersion.map(DatasetVersion::getVersionId).orElse(null);
        
        List<DatasetVersionDTO> versionDTOs = versionsPage.getContent().stream()
            .map(version -> convertToDatasetVersionDTO(version, version.getVersionId().equals(latestVersionId)))
            .collect(Collectors.toList());
        
        return new PagedResponseDTO<>(
            versionDTOs,
            new PagedResponseDTO.PageInfo(
                versionsPage.getNumber(),
                versionsPage.getSize(),
                versionsPage.getTotalElements(),
                versionsPage.getTotalPages(),
                versionsPage.isFirst(),
                versionsPage.isLast(),
                versionsPage.getNumberOfElements()
            )
        );
    }
    
    @Override
    public Optional<DatasetVersionDTO> getDatasetVersionById(Integer versionId) {
        return datasetVersionRepository.findById(versionId)
            .map(version -> convertToDatasetVersionDTO(version, false));
    }
    
    @Override
    public DatasetVersionDTO publishDatasetVersion(Integer versionId) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
            .orElseThrow(() -> new RuntimeException("数据集版本不存在: " + versionId));
        
        version.setIsPublished(true);
        version.setReleaseDate(java.time.LocalDate.now());
        version = datasetVersionRepository.save(version);
        
        return convertToDatasetVersionDTO(version, false);
    }
    
    @Override
    public Optional<DatasetVersionDTO> getLatestDatasetVersion() {
        return datasetVersionRepository.findLatestVersion()
            .map(version -> convertToDatasetVersionDTO(version, true));
    }
    
    @Override
    public DatasetVersionDTO updateDatasetVersion(Integer versionId, UpdateDatasetVersionRequest request) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
            .orElseThrow(() -> new RuntimeException("数据集版本不存在: " + versionId));
        
        // 如果要更新版本名称，检查名称是否已存在（除了当前版本）
        if (request.getVersionName() != null && !request.getVersionName().equals(version.getName())) {
            if (datasetVersionRepository.existsByName(request.getVersionName())) {
                throw new RuntimeException("版本名称已存在: " + request.getVersionName());
            }
            version.setName(request.getVersionName());
        }
        
        // 更新基本属性
        if (request.getDescription() != null) {
            version.setDescription(request.getDescription());
        }
        
        if (request.getReleaseDate() != null) {
            version.setReleaseDate(request.getReleaseDate());
        }
        
        // 更新发布状态
        if (request.getIsPublished() != null) {
            version.setIsPublished(request.getIsPublished());
            if (request.getIsPublished() && version.getReleaseDate() == null) {
                version.setReleaseDate(java.time.LocalDate.now());
            }
        }
        
        // 更新问题列表
        if (request.getQuestionIds() != null) {
            Set<StandardQuestion> questions = standardQuestionRepository.findAllById(request.getQuestionIds())
                    .stream()
                    .collect(Collectors.toSet());
            version.setQuestions(questions);
            version.setQuestionCount(questions.size());
        }
        
        version.setUpdatedAt(java.time.LocalDateTime.now());
        version = datasetVersionRepository.save(version);
        
        return convertToDatasetVersionDTO(version, false);
    }
    
    @Override
    public void deleteDatasetVersion(Integer versionId) {
        if (!datasetVersionRepository.existsById(versionId)) {
            throw new RuntimeException("数据集版本不存在: " + versionId);
        }
        datasetVersionRepository.deleteById(versionId);
    }      @Override
    public List<StandardQuestionVersionDTO> getQuestionVersionHistory(Integer questionId) {
        List<StandardQuestionVersion> versions = questionVersionRepository
            .findByStandardQuestionIdOrderByVersionDesc(questionId);
        
        return versions.stream()
            .map(this::convertToQuestionVersionDTO)
            .collect(Collectors.toList());
    }
      @Override
    public PagedResponseDTO<StandardQuestionVersionDTO> getQuestionVersionHistory(Integer questionId, Pageable pageable) {
        Page<StandardQuestionVersion> versionsPage = questionVersionRepository
            .findByStandardQuestionIdOrderByVersionDesc(questionId, pageable);
        
        List<StandardQuestionVersionDTO> versionDTOs = versionsPage.getContent().stream()
            .map(this::convertToQuestionVersionDTO)
            .collect(Collectors.toList());
        
        return new PagedResponseDTO<>(
            versionDTOs,
            new PagedResponseDTO.PageInfo(
                versionsPage.getNumber(),
                versionsPage.getSize(),
                versionsPage.getTotalElements(),
                versionsPage.getTotalPages(),
                versionsPage.isFirst(),
                versionsPage.isLast(),
                versionsPage.getNumberOfElements()
            )
        );
    }
      @Override
    public Optional<StandardQuestionVersionDTO> getQuestionVersionById(Integer versionId) {
        return questionVersionRepository.findById(versionId)
            .map(this::convertToQuestionVersionDTO);
    }
      @Override
    public Optional<StandardQuestionVersionDTO> getLatestQuestionVersion(Integer questionId) {
        return questionVersionRepository.findLatestVersionByQuestionId(questionId)
            .map(this::convertToQuestionVersionDTO);
    }    @Override
    public StandardQuestionVersionDTO rollbackQuestionToVersion(Integer questionId, Integer targetVersion) {
        // 查找目标版本
        StandardQuestionVersion targetVersionEntity = questionVersionRepository
            .findByStandardQuestionIdAndVersion(questionId, targetVersion)
            .orElseThrow(() -> new RuntimeException("目标版本不存在: " + targetVersion));
        
        // 获取当前最大版本号并创建新版本
        Integer nextVersion = questionVersionRepository.findMaxVersionByQuestionId(questionId)
            .map(maxVersion -> maxVersion + 1)
            .orElse(1);
        
        // 创建回滚版本
        StandardQuestionVersion rollbackVersion = new StandardQuestionVersion();
        rollbackVersion.setStandardQuestionId(questionId);
        rollbackVersion.setQuestion(targetVersionEntity.getQuestion());
        rollbackVersion.setCategoryId(targetVersionEntity.getCategoryId());
        rollbackVersion.setQuestionType(targetVersionEntity.getQuestionType());
        rollbackVersion.setDifficulty(targetVersionEntity.getDifficulty());
        rollbackVersion.setVersion(nextVersion);
        rollbackVersion.setChangeReason("回滚到版本 " + targetVersion);
        // TODO: 设置当前用户ID
        
        rollbackVersion = questionVersionRepository.save(rollbackVersion);
          // 更新主表
        StandardQuestion question = standardQuestionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("标准问题不存在: " + questionId));
        
        question.setQuestion(targetVersionEntity.getQuestion());
        
        // 设置category对象而不是categoryId
        if (targetVersionEntity.getCategoryId() != null) {
            QuestionCategory category = new QuestionCategory();
            category.setCategoryId(targetVersionEntity.getCategoryId());
            question.setCategory(category);
        }
        
        question.setQuestionType(targetVersionEntity.getQuestionType());
        question.setDifficulty(targetVersionEntity.getDifficulty());
        question.setVersion(nextVersion);
        
        standardQuestionRepository.save(question);
        
        return convertToQuestionVersionDTO(rollbackVersion);
    }
    
    @Override
    public VersionStatisticsDTO getVersionStatistics() {
        long totalDatasetVersions = datasetVersionRepository.count();
        long publishedDatasetVersions = datasetVersionRepository.countPublishedDatasetVersions();
        long totalQuestionVersions = questionVersionRepository.count();
        
        // 计算有多个版本的问题数量
        // TODO: 实现更精确的统计查询
        long questionsWithMultipleVersions = 0;
        
        String latestDatasetVersion = datasetVersionRepository.findLatestVersion()
            .map(DatasetVersion::getName)
            .orElse("无");
        
        // 计算本月和今天的版本变更数量
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        
        List<StandardQuestionVersion> monthlyChanges = questionVersionRepository
            .findVersionsInTimeRange(startOfMonth, now);
        List<StandardQuestionVersion> dailyChanges = questionVersionRepository
            .findVersionsInTimeRange(startOfDay, now);
        
        return new VersionStatisticsDTO(
            totalDatasetVersions,
            publishedDatasetVersions,
            totalQuestionVersions,
            questionsWithMultipleVersions,
            latestDatasetVersion,
            (long) monthlyChanges.size(),
            (long) dailyChanges.size()
        );
    }
    
    @Override
    public List<StandardQuestionVersionDTO> getVersionChangeHistory(LocalDateTime startTime, LocalDateTime endTime) {
        List<StandardQuestionVersion> versions = questionVersionRepository
            .findVersionsInTimeRange(startTime, endTime);
        
        return versions.stream()
            .map(this::convertToQuestionVersionDTO)
            .collect(Collectors.toList());
    }
    
    // 辅助方法
    
    private DatasetVersionDTO convertToDatasetVersionDTO(DatasetVersion version, boolean isLatest) {
        DatasetVersionDTO dto = new DatasetVersionDTO();
        dto.setVersionId(version.getVersionId());
        dto.setName(version.getName());
        dto.setDescription(version.getDescription());
        dto.setReleaseDate(version.getReleaseDate());
        dto.setIsPublished(version.getIsPublished());
        dto.setQuestionCount(version.getQuestionCount());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setUpdatedAt(version.getUpdatedAt());
        dto.setIsLatest(isLatest);
        return dto;
    }
    
    private StandardQuestionVersionDTO convertToQuestionVersionDTO(StandardQuestionVersion version) {
        StandardQuestionVersionDTO dto = new StandardQuestionVersionDTO();
        dto.setVersionId(version.getVersionId());
        dto.setStandardQuestionId(version.getStandardQuestionId());
        dto.setQuestion(version.getQuestion());
        dto.setCategoryId(version.getCategoryId());
        dto.setQuestionType(version.getQuestionType() != null ? version.getQuestionType().name() : null);
        dto.setDifficulty(version.getDifficulty() != null ? version.getDifficulty().name() : null);
        dto.setVersion(version.getVersion());
        dto.setChangedBy(version.getChangedBy());
        dto.setChangeReason(version.getChangeReason());
        dto.setCreatedAt(version.getCreatedAt());
          // 判断是否为最新版本
        Optional<Integer> maxVersion = questionVersionRepository
            .findMaxVersionByQuestionId(version.getStandardQuestionId());
        dto.setIsLatest(maxVersion.map(max -> max.equals(version.getVersion())).orElse(false));
          return dto;
    }
}
