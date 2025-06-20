package com.llm.eval.service.impl;

import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.dto.DatasetVersionDTO;
import com.llm.eval.dto.PagedResponseDTO;
import com.llm.eval.repository.DatasetVersionRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.service.DatasetVersionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DatasetVersionServiceImpl implements DatasetVersionService {
    
    private final DatasetVersionRepository datasetVersionRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    
    @Autowired
    public DatasetVersionServiceImpl(
            DatasetVersionRepository datasetVersionRepository,
            StandardQuestionRepository standardQuestionRepository) {
        this.datasetVersionRepository = datasetVersionRepository;
        this.standardQuestionRepository = standardQuestionRepository;
    }
    
    @Override
    public List<DatasetVersion> getAllDatasetVersions() {
        return datasetVersionRepository.findAll();
    }
    
    @Override
    public List<DatasetVersion> getPublishedDatasetVersions() {
        return datasetVersionRepository.findByIsPublishedTrue();
    }
    
    @Override
    public Optional<DatasetVersion> getDatasetVersionById(Integer id) {
        return datasetVersionRepository.findById(id);
    }
    
    @Override
    public Set<StandardQuestion> getQuestionsInDatasetVersion(Integer versionId) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + versionId));
        
        // 获取问题集合
        Set<StandardQuestion> questions = version.getQuestions();
        
        // 确保标准答案被加载
        if (questions != null && !questions.isEmpty()) {
            // 对每个问题手动加载标准答案
            for (StandardQuestion question : questions) {
                // 使用Hibernate.initialize确保集合被加载
                org.hibernate.Hibernate.initialize(question.getStandardAnswers());
            }
        }
        
        return questions;
    }
    
    @Override
    @Transactional
    public DatasetVersion createDatasetVersion(DatasetVersion datasetVersion) {
        // Set default values
        if (datasetVersion.getCreatedAt() == null) {
            datasetVersion.setCreatedAt(LocalDateTime.now());
        }
        datasetVersion.setUpdatedAt(LocalDateTime.now());
        
        // Default to not published
        if (datasetVersion.getIsPublished() == null) {
            datasetVersion.setIsPublished(false);
        }
        
        // Initialize question count
        if (datasetVersion.getQuestions() != null) {
            datasetVersion.setQuestionCount(datasetVersion.getQuestions().size());
        } else {
            datasetVersion.setQuestionCount(0);
            datasetVersion.setQuestions(new HashSet<>());
        }
        
        return datasetVersionRepository.save(datasetVersion);
    }
    
    @Override
    @Transactional
    public DatasetVersion updateDatasetVersion(Integer id, DatasetVersion datasetVersion) {
        DatasetVersion existingVersion = datasetVersionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + id));
        
        // Update basic properties
        existingVersion.setName(datasetVersion.getName());
        existingVersion.setDescription(datasetVersion.getDescription());
        existingVersion.setReleaseDate(datasetVersion.getReleaseDate());
        existingVersion.setUpdatedAt(LocalDateTime.now());
        
        // Don't update isPublished or questions here
        
        return datasetVersionRepository.save(existingVersion);
    }
    
    @Override
    @Transactional
    public DatasetVersion addQuestionsToDatasetVersion(Integer versionId, Set<Integer> questionIds) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + versionId));
        
        Set<StandardQuestion> questionsToAdd = standardQuestionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toSet());
        
        // Create a new set if it's null
        if (version.getQuestions() == null) {
            version.setQuestions(new HashSet<>());
        }
        
        // Add all questions
        version.getQuestions().addAll(questionsToAdd);
        
        // Update question count
        version.setQuestionCount(version.getQuestions().size());
        version.setUpdatedAt(LocalDateTime.now());
        
        return datasetVersionRepository.save(version);
    }
    
    @Override
    @Transactional
    public DatasetVersion removeQuestionsFromDatasetVersion(Integer versionId, Set<Integer> questionIds) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + versionId));
        
        // If no questions, nothing to remove
        if (version.getQuestions() == null || version.getQuestions().isEmpty()) {
            return version;
        }
        
        // Remove the specified questions
        version.setQuestions(
                version.getQuestions().stream()
                        .filter(q -> !questionIds.contains(q.getStandardQuestionId()))
                        .collect(Collectors.toSet())
        );
        
        // Update question count
        version.setQuestionCount(version.getQuestions().size());
        version.setUpdatedAt(LocalDateTime.now());
        
        return datasetVersionRepository.save(version);
    }
    
    @Override
    @Transactional
    public DatasetVersion publishDatasetVersion(Integer id) {
        DatasetVersion version = datasetVersionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + id));
        
        // Cannot publish a version with no questions
        if (version.getQuestions() == null || version.getQuestions().isEmpty()) {
            throw new IllegalStateException("Cannot publish a dataset version with no questions");
        }
        
        // Set published state
        version.setIsPublished(true);
        
        // Set release date if not already set
        if (version.getReleaseDate() == null) {
            version.setReleaseDate(LocalDate.now());
        }
        
        version.setUpdatedAt(LocalDateTime.now());
        
        return datasetVersionRepository.save(version);
    }
    
    @Override
    @Transactional
    public void deleteDatasetVersion(Integer id) {
        datasetVersionRepository.deleteById(id);
    }
    
    // 添加缺少的版本管理方法
    
    @Override
    @Transactional
    public DatasetVersion unpublishDatasetVersion(Integer id) {
        DatasetVersion version = datasetVersionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + id));
        
        version.setIsPublished(false);
        version.setUpdatedAt(LocalDateTime.now());
        
        return datasetVersionRepository.save(version);
    }
    
    @Override
    public PagedResponseDTO<DatasetVersionDTO> getVersionsPaged(Pageable pageable) {
        Page<DatasetVersion> page = datasetVersionRepository.findAll(pageable);
        List<DatasetVersionDTO> dtos = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return PagedResponseDTO.fromPageWithMapper(page, dtos);
    }
    
    @Override
    public PagedResponseDTO<DatasetVersionDTO> getVersionsByPublishStatus(Boolean isPublished, Pageable pageable) {
        Page<DatasetVersion> page = datasetVersionRepository.findByIsPublished(isPublished, pageable);
        List<DatasetVersionDTO> dtos = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return PagedResponseDTO.fromPageWithMapper(page, dtos);
    }
    
    @Override
    public DatasetVersionDTO getVersionDTOById(Integer versionId) {
        DatasetVersion version = datasetVersionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Dataset version not found with id: " + versionId));
        return convertToDTO(version);
    }
    
    @Override
    @Transactional
    public DatasetVersionDTO createVersionEnhanced(String name, String description, List<Integer> questionIds, Integer baseVersionId) {
        // 检查版本名称是否已存在
        if (isVersionNameExists(name)) {
            throw new IllegalArgumentException("Version name already exists: " + name);
        }
        
        // 创建新版本
        DatasetVersion newVersion = new DatasetVersion();
        newVersion.setName(name);
        newVersion.setDescription(description);
        newVersion.setCreatedAt(LocalDateTime.now());
        newVersion.setUpdatedAt(LocalDateTime.now());
        newVersion.setIsPublished(false);
          // 如果指定了基础版本，复制其信息
        if (baseVersionId != null) {
            DatasetVersion baseVersion = datasetVersionRepository.findById(baseVersionId)
                    .orElseThrow(() -> new EntityNotFoundException("Base version not found with id: " + baseVersionId));
            // 由于DatasetVersion没有version字段，我们可以在描述中说明这是基于其他版本的
            if (description == null || description.isEmpty()) {
                newVersion.setDescription("Based on version: " + baseVersion.getName());
            }
        }
        
        // 保存版本
        newVersion = datasetVersionRepository.save(newVersion);
        
        // 添加问题
        if (questionIds != null && !questionIds.isEmpty()) {
            Set<Integer> questionIdSet = new HashSet<>(questionIds);
            newVersion = addQuestionsToDatasetVersion(newVersion.getVersionId(), questionIdSet);
        }
        
        return convertToDTO(newVersion);
    }
    
    @Override
    public boolean isVersionNameExists(String name) {
        return datasetVersionRepository.existsByName(name);
    }
    
    @Override
    public DatasetVersionDTO getLatestPublishedVersion() {
        List<DatasetVersion> publishedVersions = datasetVersionRepository.findByIsPublishedTrueOrderByCreatedAtDesc();
        if (publishedVersions.isEmpty()) {
            throw new EntityNotFoundException("No published dataset version found");
        }
        return convertToDTO(publishedVersions.get(0));
    }
    
    @Override
    @Transactional
    public DatasetVersion createDatasetVersionWithQuestions(DatasetVersion datasetVersion, List<Integer> questionIds) {
        // 先创建数据集版本
        DatasetVersion createdVersion = createDatasetVersion(datasetVersion);
        
        if (questionIds != null && !questionIds.isEmpty()) {
            // 将List转换为Set
            Set<Integer> questionIdSet = new HashSet<>(questionIds);
            
            // 添加问题到数据集版本
            try {
                System.out.println("添加 " + questionIdSet.size() + " 个问题到数据集版本 " + createdVersion.getVersionId());
                return addQuestionsToDatasetVersion(createdVersion.getVersionId(), questionIdSet);
            } catch (Exception e) {
                System.err.println("添加问题到数据集版本失败: " + e.getMessage());
                e.printStackTrace();
                // 即使添加问题失败，也返回已创建的数据集版本
                return createdVersion;
            }
        }
        
        return createdVersion;
    }
    
    @Override
    public PagedResponseDTO<DatasetVersionDTO> searchVersionsByName(String keyword, Pageable pageable) {
        // 使用Spring Data JPA的模糊查询
        Page<DatasetVersion> page = datasetVersionRepository.findByNameContainingIgnoreCase(keyword, pageable);
        
        // 转换为DTO
        List<DatasetVersionDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return PagedResponseDTO.fromPageWithMapper(page, dtoList);
    }
    
    @Override
    public PagedResponseDTO<DatasetVersionDTO> searchVersionsByNameAndPublishStatus(
            String keyword, Boolean isPublished, Pageable pageable) {
        // 使用Spring Data JPA的模糊查询和状态筛选
        Page<DatasetVersion> page = datasetVersionRepository.findByNameContainingIgnoreCaseAndIsPublished(
                keyword, isPublished, pageable);
        
        // 转换为DTO
        List<DatasetVersionDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return PagedResponseDTO.fromPageWithMapper(page, dtoList);
    }
    
    private DatasetVersionDTO convertToDTO(DatasetVersion version) {
        if (version == null) {
            return null;
        }

        System.out.println("转换数据集版本为DTO: id=" + version.getVersionId() + ", name=" + version.getName());
        
        DatasetVersionDTO dto = new DatasetVersionDTO();
        dto.setVersionId(version.getVersionId());
        dto.setName(version.getName());
        dto.setDescription(version.getDescription());
        dto.setReleaseDate(version.getReleaseDate());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setUpdatedAt(version.getUpdatedAt());
        dto.setQuestionCount(version.getQuestionCount());
        dto.setIsPublished(version.getIsPublished());
        dto.setIsLatest(isLatestVersion(version));
        
        // 设置问题IDs
        if (version.getQuestions() != null) {
            Set<Integer> questionIds = version.getQuestions().stream()
                    .map(q -> q.getStandardQuestionId())
                    .collect(Collectors.toSet());
            dto.setQuestionIds(questionIds);
            System.out.println("包含问题数量: " + questionIds.size());
        }
        
        return dto;
    }
    
    // 辅助方法：判断是否为最新版本
    private boolean isLatestVersion(DatasetVersion version) {
        List<DatasetVersion> allVersions = datasetVersionRepository.findAllByOrderByCreatedAtDesc();
        return !allVersions.isEmpty() && allVersions.get(0).getVersionId().equals(version.getVersionId());
    }
}