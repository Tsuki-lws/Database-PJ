package com.llm.eval.controller;

import com.llm.eval.dto.*;
import com.llm.eval.service.VersionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VersionManagementController {
    
    @Autowired
    private VersionManagementService versionManagementService;
    
    // 数据集版本管理端点
    
    /**
     * 创建数据集版本
     */
    @PostMapping("/dataset-versions")
    public ResponseEntity<Map<String, Object>> createDatasetVersion(@RequestBody CreateDatasetVersionRequest request) {
        try {
            DatasetVersionDTO versionDTO = versionManagementService.createDatasetVersion(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "数据集版本创建成功",
                "versionId", versionDTO.getVersionId(),
                "versionInfo", versionDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "创建失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取数据集版本列表
     */
    @GetMapping("/dataset-versions")
    public ResponseEntity<PagedResponseDTO<DatasetVersionDTO>> getDatasetVersions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponseDTO<DatasetVersionDTO> result = versionManagementService.getDatasetVersions(pageable);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取数据集版本详情
     */
    @GetMapping("/dataset-versions/{versionId}")
    public ResponseEntity<DatasetVersionDTO> getDatasetVersionById(@PathVariable Integer versionId) {
        return versionManagementService.getDatasetVersionById(versionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 发布数据集版本
     */
    @PostMapping("/dataset-versions/{versionId}/publish")
    public ResponseEntity<Map<String, Object>> publishDatasetVersion(@PathVariable Integer versionId) {
        try {
            DatasetVersionDTO versionDTO = versionManagementService.publishDatasetVersion(versionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "数据集版本发布成功",
                "versionInfo", versionDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "发布失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取最新数据集版本
     */
    @GetMapping("/dataset-versions/latest")
    public ResponseEntity<DatasetVersionDTO> getLatestDatasetVersion() {
        return versionManagementService.getLatestDatasetVersion()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 删除数据集版本
     */
    @DeleteMapping("/dataset-versions/{versionId}")
    public ResponseEntity<Map<String, Object>> deleteDatasetVersion(@PathVariable Integer versionId) {
        try {
            versionManagementService.deleteDatasetVersion(versionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "数据集版本删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除失败: " + e.getMessage()
            ));
        }
    }
      // 标准问题版本管理端点（查询功能）
      /**
     * 获取标准问题版本历史（版本管理视图）
     */
    @GetMapping("/version-management/questions/{questionId}/versions")
    public ResponseEntity<Map<String, Object>> getQuestionVersionHistory(
            @PathVariable Integer questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean paginated) {
        
        if (paginated) {
            Pageable pageable = PageRequest.of(page, size);
            PagedResponseDTO<StandardQuestionVersionDTO> result = versionManagementService
                .getQuestionVersionHistory(questionId, pageable);
            
            return ResponseEntity.ok(Map.of(
                "questionId", questionId,
                "currentVersion", result.getContent().isEmpty() ? "v1.0" : 
                    "v" + result.getContent().get(0).getVersionId(),
                "versions", result
            ));
        } else {
            List<StandardQuestionVersionDTO> versions = versionManagementService
                .getQuestionVersionHistory(questionId);
            
            return ResponseEntity.ok(Map.of(
                "questionId", questionId,
                "currentVersion", versions.isEmpty() ? "v1.0" : "v" + versions.get(0).getVersionId(),
                "versions", versions
            ));
        }
    }
      /**
     * 获取标准问题版本详情（版本管理视图）
     */
    @GetMapping("/version-management/question-versions/{versionId}")
    public ResponseEntity<StandardQuestionVersionDTO> getQuestionVersionById(@PathVariable Integer versionId) {
        return versionManagementService.getQuestionVersionById(versionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取标准问题最新版本（版本管理视图）
     */
    @GetMapping("/version-management/questions/{questionId}/versions/latest")
    public ResponseEntity<StandardQuestionVersionDTO> getLatestQuestionVersion(@PathVariable Integer questionId) {
        return versionManagementService.getLatestQuestionVersion(questionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
      
    /**
     * 回滚标准问题到指定版本
     */
    @PostMapping("/standard-questions/{questionId}/versions/{targetVersion}/rollback")
    public ResponseEntity<Map<String, Object>> rollbackQuestionToVersion(
            @PathVariable Integer questionId,
            @PathVariable Integer targetVersion) {
        
        try {
            StandardQuestionVersionDTO versionDTO = versionManagementService
                .rollbackQuestionToVersion(questionId, targetVersion);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "回滚成功",
                "versionInfo", versionDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "回滚失败: " + e.getMessage()
            ));
        }
    }
    
    // 统计和查询端点
    
    /**
     * 获取版本统计信息
     */
    @GetMapping("/versions/statistics")
    public ResponseEntity<VersionStatisticsDTO> getVersionStatistics() {
        VersionStatisticsDTO statistics = versionManagementService.getVersionStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取版本变更历史
     */
    @GetMapping("/versions/changes")
    public ResponseEntity<List<StandardQuestionVersionDTO>> getVersionChangeHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<StandardQuestionVersionDTO> changes = versionManagementService
            .getVersionChangeHistory(startTime, endTime);
        
        return ResponseEntity.ok(changes);
    }
}
