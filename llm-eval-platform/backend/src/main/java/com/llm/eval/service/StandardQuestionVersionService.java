package com.llm.eval.service;

import com.llm.eval.dto.PagedResponseDTO;
import com.llm.eval.dto.StandardQuestionVersionDTO;
import com.llm.eval.dto.VersionComparisonDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 标准问题版本管理服务接口
 */
public interface StandardQuestionVersionService {
    
    /**
     * 创建标准问题的新版本
     * @param questionId 标准问题ID
     * @param versionName 版本名称
     * @param changeReason 修改原因
     * @param questionTitle 问题标题
     * @param questionBody 问题内容
     * @param standardAnswer 标准答案
     * @param referenceAnswers 参考答案列表
     * @param changedBy 修改人ID
     * @return 创建的版本信息
     */
    StandardQuestionVersionDTO createQuestionVersion(Long questionId, String versionName, String changeReason, 
                                                   String questionTitle, String questionBody, 
                                                   String standardAnswer, List<String> referenceAnswers, 
                                                   Long changedBy);
    
    /**
     * 获取标准问题的版本历史
     * @param questionId 标准问题ID
     * @return 版本历史列表
     */
    List<StandardQuestionVersionDTO> getQuestionVersionHistory(Long questionId);
    
    /**
     * 获取标准问题的版本历史（分页）
     * @param questionId 标准问题ID
     * @param pageable 分页参数
     * @return 分页的版本历史
     */
    PagedResponseDTO<StandardQuestionVersionDTO> getQuestionVersionHistoryPaged(Long questionId, Pageable pageable);
    
    /**
     * 根据版本ID获取版本详情
     * @param versionId 版本ID
     * @return 版本详情
     */
    StandardQuestionVersionDTO getVersionById(Long versionId);
    
    /**
     * 获取标准问题的最新版本
     * @param questionId 标准问题ID
     * @return 最新版本信息
     */
    StandardQuestionVersionDTO getLatestVersion(Long questionId);
    
    /**
     * 比较两个版本的差异
     * @param fromVersionId 源版本ID
     * @param toVersionId 目标版本ID
     * @return 版本比较结果
     */
    VersionComparisonDTO compareVersions(Long fromVersionId, Long toVersionId);
    
    /**
     * 获取下一个版本号
     * @param questionId 标准问题ID
     * @return 下一个版本号
     */
    Integer getNextVersionNumber(Long questionId);
    
    /**
     * 统计标准问题的版本数量
     * @param questionId 标准问题ID
     * @return 版本数量
     */
    Long countVersionsByQuestionId(Long questionId);
    
    /**
     * 根据修改人获取版本列表
     * @param changedBy 修改人ID
     * @return 版本列表
     */
    List<StandardQuestionVersionDTO> getVersionsByChangedBy(Long changedBy);
    
    /**
     * 获取指定时间范围内的版本
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 版本列表
     */
    List<StandardQuestionVersionDTO> getVersionsInTimeRange(java.time.LocalDateTime startTime, 
                                                           java.time.LocalDateTime endTime);
    
    /**
     * 回滚到指定版本
     * @param questionId 标准问题ID
     * @param targetVersionId 目标版本ID
     * @param changeReason 回滚原因
     * @param changedBy 操作人ID
     * @return 新创建的版本信息
     */
    StandardQuestionVersionDTO rollbackToVersion(Long questionId, Long targetVersionId, 
                                                String changeReason, Long changedBy);
    
    /**
     * 删除版本（软删除）
     * @param versionId 版本ID
     */
    void deleteVersion(Long versionId);
}
