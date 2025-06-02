package com.llmeval.service;

import com.llmeval.dto.EvaluationDetailDTO;
import com.llmeval.dto.ManualEvaluationDTO;
import com.llmeval.dto.UnevaluatedAnswerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 评测服务接口
 */
public interface EvaluationService {

    /**
     * 获取待评测的答案列表
     */
    Page<UnevaluatedAnswerDTO> getUnevaluatedAnswers(Integer modelId, String questionType, Integer categoryId, Pageable pageable);

    /**
     * 获取评测详情（用于人工评测）
     */
    EvaluationDetailDTO getEvaluationDetail(Integer answerId);

    /**
     * 提交人工评测结果
     */
    void submitManualEvaluation(ManualEvaluationDTO evaluationDTO);

    /**
     * 导入评测结果
     */
    Map<String, Object> importEvaluations(MultipartFile file);

    /**
     * 获取模型对比数据
     */
    Map<String, Object> getModelComparisonData(List<Integer> modelIds);
} 