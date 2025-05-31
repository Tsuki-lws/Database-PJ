package com.llm.eval.service;

import com.llm.eval.dto.DatasetVersionDTO;
import com.llm.eval.dto.PagedResponseDTO;
import com.llm.eval.model.DatasetVersion;
import com.llm.eval.model.StandardQuestion;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DatasetVersionService {
    
    /**
     * Get all dataset versions
     * 
     * @return List of all dataset versions
     */
    List<DatasetVersion> getAllDatasetVersions();
    
    /**
     * Get published dataset versions
     * 
     * @return List of published dataset versions
     */
    List<DatasetVersion> getPublishedDatasetVersions();
    
    /**
     * Get a dataset version by ID
     * 
     * @param id The ID of the dataset version
     * @return The dataset version if found
     */
    Optional<DatasetVersion> getDatasetVersionById(Integer id);
    
    /**
     * Get questions in a dataset version
     * 
     * @param versionId The ID of the dataset version
     * @return List of questions in the dataset version
     */
    Set<StandardQuestion> getQuestionsInDatasetVersion(Integer versionId);
    
    /**
     * Create a new dataset version
     * 
     * @param datasetVersion The dataset version to create
     * @return The created dataset version
     */
    DatasetVersion createDatasetVersion(DatasetVersion datasetVersion);
    
    /**
     * Update a dataset version
     * 
     * @param id The ID of the dataset version to update
     * @param datasetVersion The updated dataset version
     * @return The updated dataset version
     */
    DatasetVersion updateDatasetVersion(Integer id, DatasetVersion datasetVersion);
    
    /**
     * Add questions to a dataset version
     * 
     * @param versionId The ID of the dataset version
     * @param questionIds The IDs of the questions to add
     * @return The updated dataset version
     */
    DatasetVersion addQuestionsToDatasetVersion(Integer versionId, Set<Integer> questionIds);
    
    /**
     * Remove questions from a dataset version
     * 
     * @param versionId The ID of the dataset version
     * @param questionIds The IDs of the questions to remove
     * @return The updated dataset version
     */
    DatasetVersion removeQuestionsFromDatasetVersion(Integer versionId, Set<Integer> questionIds);
    
    /**
     * Publish a dataset version
     * 
     * @param id The ID of the dataset version to publish
     * @return The published dataset version
     */
    DatasetVersion publishDatasetVersion(Integer id);
    
    /**
     * Delete a dataset version
     * 
     * @param id The ID of the dataset version to delete
     */
    void deleteDatasetVersion(Integer id);
    
    // 新增版本管理方法
    
    /**
     * 获取数据集版本列表（分页）
     * @param pageable 分页参数
     * @return 分页的版本列表
     */
    PagedResponseDTO<DatasetVersionDTO> getVersionsPaged(Pageable pageable);
    
    /**
     * 根据发布状态获取版本列表
     * @param isPublished 是否已发布
     * @param pageable 分页参数
     * @return 分页的版本列表
     */
    PagedResponseDTO<DatasetVersionDTO> getVersionsByPublishStatus(Boolean isPublished, Pageable pageable);
    
    /**
     * 根据版本ID获取版本详情DTO
     * @param versionId 版本ID
     * @return 版本详情DTO
     */
    DatasetVersionDTO getVersionDTOById(Integer versionId);
    
    /**
     * 创建新的数据集版本（增强版）
     * @param name 版本名称
     * @param description 版本描述
     * @param questionIds 包含的问题ID列表
     * @param baseVersionId 基础版本ID（可选）
     * @return 创建的版本信息
     */
    DatasetVersionDTO createVersionEnhanced(String name, String description, List<Integer> questionIds, Integer baseVersionId);
    
    /**
     * 检查版本名称是否已存在
     * @param name 版本名称
     * @return 是否存在
     */
    boolean isVersionNameExists(String name);
    
    /**
     * 获取最新发布的版本
     * @return 最新发布的版本信息
     */
    DatasetVersionDTO getLatestPublishedVersion();
    
    /**
     * 取消发布数据集版本
     * @param id 版本ID
     * @return 取消发布后的版本信息
     */
    DatasetVersion unpublishDatasetVersion(Integer id);
}