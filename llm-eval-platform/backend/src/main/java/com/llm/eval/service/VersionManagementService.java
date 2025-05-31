package com.llm.eval.service;

import com.llm.eval.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VersionManagementService {
    
    // 数据集版本管理
    
    /**
     * 创建新的数据集版本
     */
    DatasetVersionDTO createDatasetVersion(CreateDatasetVersionRequest request);
    
    /**
     * 获取数据集版本列表（分页）
     */
    PagedResponseDTO<DatasetVersionDTO> getDatasetVersions(Pageable pageable);
    
    /**
     * 根据ID获取数据集版本详情
     */
    Optional<DatasetVersionDTO> getDatasetVersionById(Integer versionId);
    
    /**
     * 发布数据集版本
     */
    DatasetVersionDTO publishDatasetVersion(Integer versionId);
    
    /**
     * 更新数据集版本
     */
    DatasetVersionDTO updateDatasetVersion(Integer versionId, UpdateDatasetVersionRequest request);
    
    /**
     * 获取最新的数据集版本
     */
    Optional<DatasetVersionDTO> getLatestDatasetVersion();
    
    /**
     * 删除数据集版本
     */
    void deleteDatasetVersion(Integer versionId);
      // 标准问题版本管理（查询功能）
    
    /**
     * 获取标准问题的版本历史
     */
    List<StandardQuestionVersionDTO> getQuestionVersionHistory(Integer questionId);
    
    /**
     * 获取标准问题的版本历史（分页）
     */
    PagedResponseDTO<StandardQuestionVersionDTO> getQuestionVersionHistory(Integer questionId, Pageable pageable);
    
    /**
     * 根据版本ID获取标准问题版本详情
     */
    Optional<StandardQuestionVersionDTO> getQuestionVersionById(Integer versionId);
    
    /**
     * 获取标准问题的最新版本
     */
    Optional<StandardQuestionVersionDTO> getLatestQuestionVersion(Integer questionId);
      /**
     * 回滚标准问题到指定版本
     */
    StandardQuestionVersionDTO rollbackQuestionToVersion(Integer questionId, Integer targetVersion);
    
    // 统计和查询方法
    
    /**
     * 获取版本统计信息
     */
    VersionStatisticsDTO getVersionStatistics();
    
    /**
     * 根据时间范围查询版本变更历史
     */
    List<StandardQuestionVersionDTO> getVersionChangeHistory(java.time.LocalDateTime startTime, 
                                                           java.time.LocalDateTime endTime);
}
