package com.llm.eval.service.impl;

import com.llm.eval.dto.PagedResponseDTO;
import com.llm.eval.dto.StandardQuestionVersionDTO;
import com.llm.eval.dto.VersionComparisonDTO;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.StandardQuestionVersion;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.StandardQuestionVersionRepository;
import com.llm.eval.service.StandardQuestionVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标准问题版本管理服务实现
 */
@Service
@Transactional
public class StandardQuestionVersionServiceImpl implements StandardQuestionVersionService {
    
    @Autowired
    private StandardQuestionVersionRepository versionRepository;
    
    @Autowired
    private StandardQuestionRepository questionRepository;
    
    @Override
    public StandardQuestionVersionDTO createQuestionVersion(Long questionId, String versionName, String changeReason,
                                                          String questionTitle, String questionBody,
                                                          String standardAnswer, List<String> referenceAnswers,
                                                          Long changedBy) {
        // 获取下一个版本号
        Integer nextVersion = getNextVersionNumber(questionId);
          // 创建新版本记录
        StandardQuestionVersion version = new StandardQuestionVersion();
        version.setStandardQuestionId(questionId.intValue());
        version.setQuestion(questionBody);
        version.setVersion(nextVersion);
        version.setChangeReason(changeReason);
        version.setChangedBy(changedBy.intValue());// 获取标准问题信息以设置其他字段
        StandardQuestion question = questionRepository.findById(questionId.intValue())
                .orElseThrow(() -> new RuntimeException("标准问题不存在: " + questionId));
          // 设置类别ID - 从category对象中获取
        if (question.getCategory() != null) {
            version.setCategoryId(question.getCategory().getCategoryId());
        }
        version.setQuestionType(question.getQuestionType());
        version.setDifficulty(question.getDifficulty());
        
        StandardQuestionVersion savedVersion = versionRepository.save(version);
        
        // 更新标准问题的当前内容
        question.setQuestion(questionBody);
        // 更新标准答案等...
        questionRepository.save(question);
        
        return convertToDTO(savedVersion);
    }
    
    @Override
    public List<StandardQuestionVersionDTO> getQuestionVersionHistory(Long questionId) {
        List<StandardQuestionVersion> versions = versionRepository
                .findByStandardQuestionIdOrderByVersionDesc(questionId);
        return versions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public PagedResponseDTO<StandardQuestionVersionDTO> getQuestionVersionHistoryPaged(Long questionId, Pageable pageable) {
        Page<StandardQuestionVersion> versionsPage = versionRepository
                .findByStandardQuestionIdOrderByVersionDesc(questionId, pageable);
          List<StandardQuestionVersionDTO> versionDTOs = versionsPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PagedResponseDTO.fromPageWithMapper(versionsPage, versionDTOs);
    }
    
    @Override
    public StandardQuestionVersionDTO getVersionById(Long versionId) {
        StandardQuestionVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("版本不存在: " + versionId));
        return convertToDTO(version);
    }
    
    @Override
    public StandardQuestionVersionDTO getLatestVersion(Long questionId) {
        StandardQuestionVersion version = versionRepository
                .findLatestVersionByQuestionId(questionId)
                .orElseThrow(() -> new RuntimeException("问题版本不存在: " + questionId));
        return convertToDTO(version);
    }
    
    @Override
    public VersionComparisonDTO compareVersions(Long fromVersionId, Long toVersionId) {
        StandardQuestionVersion fromVersion = versionRepository.findById(fromVersionId)
                .orElseThrow(() -> new RuntimeException("源版本不存在: " + fromVersionId));
        StandardQuestionVersion toVersion = versionRepository.findById(toVersionId)
                .orElseThrow(() -> new RuntimeException("目标版本不存在: " + toVersionId));
        
        // 创建版本信息
        VersionComparisonDTO.VersionInfo fromInfo = new VersionComparisonDTO.VersionInfo(
                fromVersion.getVersionId(),
                "v" + fromVersion.getVersion(),
                fromVersion.getCreatedAt().toString()
        );
        
        VersionComparisonDTO.VersionInfo toInfo = new VersionComparisonDTO.VersionInfo(
                toVersion.getVersionId(),
                "v" + toVersion.getVersion(),
                toVersion.getCreatedAt().toString()
        );
        
        // 比较差异
        Map<String, VersionComparisonDTO.FieldDifference> differences = new HashMap<>();
        
        // 比较问题内容
        boolean questionChanged = !fromVersion.getQuestion().equals(toVersion.getQuestion());
        differences.put("question", new VersionComparisonDTO.FieldDifference(
                questionChanged, fromVersion.getQuestion(), toVersion.getQuestion()));
        
        // 比较分类
        boolean categoryChanged = !fromVersion.getCategoryId().equals(toVersion.getCategoryId());
        differences.put("categoryId", new VersionComparisonDTO.FieldDifference(
                categoryChanged, fromVersion.getCategoryId().toString(), toVersion.getCategoryId().toString()));
        
        // 比较修改原因
        boolean reasonChanged = !fromVersion.getChangeReason().equals(toVersion.getChangeReason());
        differences.put("changeReason", new VersionComparisonDTO.FieldDifference(
                reasonChanged, fromVersion.getChangeReason(), toVersion.getChangeReason()));
        
        return new VersionComparisonDTO(fromInfo, toInfo, differences);
    }
    
    @Override
    public Integer getNextVersionNumber(Long questionId) {
        Integer maxVersion = versionRepository.findMaxVersionByQuestionId(questionId)
                .orElse(0);
        return maxVersion + 1;
    }
    
    @Override
    public Long countVersionsByQuestionId(Long questionId) {
        return versionRepository.countVersionsByQuestionId(questionId);
    }
    
    @Override
    public List<StandardQuestionVersionDTO> getVersionsByChangedBy(Long changedBy) {
        List<StandardQuestionVersion> versions = versionRepository
                .findByChangedByOrderByCreatedAtDesc(changedBy);
        return versions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StandardQuestionVersionDTO> getVersionsInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<StandardQuestionVersion> versions = versionRepository
                .findVersionsInTimeRange(startTime, endTime);
        return versions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public StandardQuestionVersionDTO rollbackToVersion(Long questionId, Long targetVersionId,
                                                      String changeReason, Long changedBy) {
        StandardQuestionVersion targetVersion = versionRepository.findById(targetVersionId)
                .orElseThrow(() -> new RuntimeException("目标版本不存在: " + targetVersionId));
        
        // 创建新版本，内容基于目标版本
        return createQuestionVersion(questionId, null, changeReason,
                null, targetVersion.getQuestion(), null, null, changedBy);
    }
    
    @Override
    public void deleteVersion(Long versionId) {
        versionRepository.deleteById(versionId);
    }
    
    /**
     * 转换为DTO
     */
    private StandardQuestionVersionDTO convertToDTO(StandardQuestionVersion version) {
        StandardQuestionVersionDTO dto = new StandardQuestionVersionDTO();
        dto.setVersionId(version.getVersionId());
        dto.setStandardQuestionId(version.getStandardQuestionId());
        dto.setQuestion(version.getQuestion());
        dto.setCategoryId(version.getCategoryId());
        dto.setQuestionType(version.getQuestionType() != null ? version.getQuestionType().toString() : null);
        dto.setDifficulty(version.getDifficulty() != null ? version.getDifficulty().toString() : null);
        dto.setVersion(version.getVersion());
        dto.setChangedBy(version.getChangedBy());
        dto.setChangeReason(version.getChangeReason());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setStatus("ACTIVE"); // 默认状态
        return dto;
    }
}
