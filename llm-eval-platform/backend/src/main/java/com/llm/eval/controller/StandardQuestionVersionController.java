package com.llm.eval.controller;

import com.llm.eval.dto.PagedResponseDTO;
import com.llm.eval.dto.StandardQuestionVersionDTO;
import com.llm.eval.dto.VersionComparisonDTO;
import com.llm.eval.model.ApiResponse;
import com.llm.eval.service.StandardQuestionVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标准问题版本管理控制器
 */
@RestController
@RequestMapping("/api/standard-questions")
@Tag(name = "Standard Question Version API", description = "标准问题版本管理接口")
@CrossOrigin(origins = "*")
public class StandardQuestionVersionController {
    
    @Autowired
    private StandardQuestionVersionService versionService;    /**
     * 创建标准问题版本
     */
    @PostMapping("/{questionId}/versions")
    @Operation(summary = "创建标准问题版本")
    public ApiResponse<Map<String, Object>> createQuestionVersion(
            @PathVariable Integer questionId,
            @RequestBody CreateVersionRequest request) {
        try {
            
            if (request.getQuestionBody() == null || request.getQuestionBody().trim().isEmpty()) {
                return ApiResponse.error("问题内容不能为空");
            }
            
            StandardQuestionVersionDTO versionDTO = versionService.createQuestionVersion(
                    questionId,
                    request.getVersionName(),
                    request.getChangeReason(),
                    request.getQuestionTitle(),
                    request.getQuestionBody(),
                    request.getStandardAnswer(),
                    request.getReferenceAnswers(),
                    request.getChangedBy()
            );
            
            Map<String, Object> data = new HashMap<>();
            data.put("versionId", versionDTO.getVersionId());
            data.put("versionInfo", versionDTO);
            data.put("message", "标准问题版本创建成功");
            
            return ApiResponse.success(data);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }
      /**
     * 获取标准问题版本历史
     */
    @GetMapping("/{questionId}/versions")
    @Operation(summary = "获取标准问题版本历史")
    public ResponseEntity<Map<String, Object>> getQuestionVersionHistory(
            @PathVariable Integer questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (size == 0) {
                // 获取所有版本
                List<StandardQuestionVersionDTO> versions = versionService.getQuestionVersionHistory(questionId);
                response.put("questionId", questionId);
                response.put("currentVersion", versions.isEmpty() ? null : "v" + versions.get(0).getVersionId());
                response.put("versions", versions);
            } else {
                // 分页获取版本
                Pageable pageable = PageRequest.of(page, size);
                PagedResponseDTO<StandardQuestionVersionDTO> pagedVersions = 
                        versionService.getQuestionVersionHistoryPaged(questionId, pageable);
                response.put("questionId", questionId);
                response.put("currentVersion", 
                        pagedVersions.getContent().isEmpty() ? null : "v" + pagedVersions.getContent().get(0).getVersionId());
                response.put("versions", pagedVersions.getContent());
                response.put("page", pagedVersions.getPage());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
      /**
     * 获取版本详情
     */
    @GetMapping("/versions/{versionId}")
    @Operation(summary = "获取版本详情")
    public ResponseEntity<StandardQuestionVersionDTO> getVersionById(@PathVariable Integer versionId) {
        try {
            StandardQuestionVersionDTO versionDTO = versionService.getVersionById(versionId);
            return ResponseEntity.ok(versionDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
      /**
     * 获取最新版本
     */
    @GetMapping("/{questionId}/versions/latest")
    @Operation(summary = "获取标准问题的最新版本")
    public ResponseEntity<StandardQuestionVersionDTO> getLatestVersion(@PathVariable Integer questionId) {
        try {
            StandardQuestionVersionDTO versionDTO = versionService.getLatestVersion(questionId);
            return ResponseEntity.ok(versionDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
      /**
     * 比较版本差异
     */
    @GetMapping("/versions/compare")
    @Operation(summary = "比较标准问题版本差异")
    public ResponseEntity<Map<String, Object>> compareVersions(
            @RequestParam Integer fromVersionId,
            @RequestParam Integer toVersionId) {
        try {
            VersionComparisonDTO comparison = versionService.compareVersions(fromVersionId, toVersionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("comparison", comparison);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "比较失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
      /**
     * 回滚到指定版本
     */
    @PostMapping("/{questionId}/versions/{targetVersionId}/rollback")
    @Operation(summary = "回滚到指定版本")
    public ResponseEntity<Map<String, Object>> rollbackToVersion(
            @PathVariable Integer questionId,
            @PathVariable Integer targetVersionId,
            @RequestBody RollbackRequest request) {
        try {
            StandardQuestionVersionDTO versionDTO = versionService.rollbackToVersion(
                    questionId,
                    targetVersionId,
                    request.getChangeReason(),
                    request.getChangedBy()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "回滚成功");
            response.put("newVersionId", versionDTO.getVersionId());
            response.put("versionInfo", versionDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "回滚失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
      /**
     * 获取版本统计信息
     */
    @GetMapping("/{questionId}/versions/stats")
    @Operation(summary = "获取标准问题版本统计信息")
    public ResponseEntity<Map<String, Object>> getVersionStats(@PathVariable Integer questionId) {        try {
            long versionCount = versionService.countVersionsByQuestionId(questionId);
            StandardQuestionVersionDTO latestVersion = versionService.getLatestVersion(questionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("questionId", questionId);
            response.put("totalVersions", versionCount);
            response.put("latestVersion", latestVersion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
      /**
     * 删除版本
     */
    @DeleteMapping("/versions/{versionId}")
    @Operation(summary = "删除版本")
    public ResponseEntity<Map<String, Object>> deleteVersion(@PathVariable Integer versionId) {
        try {
            versionService.deleteVersion(versionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "版本删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }    }
    
    // 请求体类
    public static class CreateVersionRequest {
        private String versionName;
        private String changeReason;
        private String questionTitle;
        private String questionBody; // 必需字段
        private String standardAnswer;
        private List<String> referenceAnswers;
        private Integer changedBy; // 必需字段：变更人ID
        
        // Getters and Setters
        public String getVersionName() { return versionName; }
        public void setVersionName(String versionName) { this.versionName = versionName; }
        
        public String getChangeReason() { return changeReason; }
        public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
        
        public String getQuestionTitle() { return questionTitle; }
        public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }
        
        public String getQuestionBody() { return questionBody; }
        public void setQuestionBody(String questionBody) { this.questionBody = questionBody; }
        
        public String getStandardAnswer() { return standardAnswer; }
        public void setStandardAnswer(String standardAnswer) { this.standardAnswer = standardAnswer; }
        
        public List<String> getReferenceAnswers() { return referenceAnswers; }
        public void setReferenceAnswers(List<String> referenceAnswers) { this.referenceAnswers = referenceAnswers; }
        
        public Integer getChangedBy() { return changedBy; }
        public void setChangedBy(Integer changedBy) { this.changedBy = changedBy; }    }
    
    public static class RollbackRequest {
        private String changeReason;
        private Integer changedBy;
        
        public String getChangeReason() { return changeReason; }
        public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
        
        public Integer getChangedBy() { return changedBy; }
        public void setChangedBy(Integer changedBy) { this.changedBy = changedBy; }
    }
}
