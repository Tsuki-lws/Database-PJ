package com.llm.eval.service;

import com.llm.eval.dto.DataImportResultDTO;
import com.llm.eval.dto.ImportTaskDTO;
import com.llm.eval.dto.RawQuestionImportDTO;
import com.llm.eval.dto.StandardQAImportDTO;
import com.llm.eval.dto.RawQuestionOnlyDTO;
import com.llm.eval.dto.RawAnswerDTO;
import com.llm.eval.dto.StandardQuestionDTO;
import com.llm.eval.dto.StandardAnswerDTO;
import com.llm.eval.dto.StandardQALinkDTO;
import com.llm.eval.model.ImportTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 数据导入服务接口
 */
public interface DataImportService {

    /**
     * 导入原始问答数据
     * @param file JSON文件
     * @return 导入结果
     */
    DataImportResultDTO importRawQA(MultipartFile file) throws IOException;

    /**
     * 导入标准问答数据
     * @param file JSON文件
     * @return 导入结果
     */
    DataImportResultDTO importStandardQA(MultipartFile file) throws IOException;

    /**
     * 导入单个原始问题
     * @param questionDTO 原始问题DTO
     * @return 导入结果
     */
    DataImportResultDTO importRawQuestion(RawQuestionImportDTO questionDTO);

    /**
     * 批量导入原始问题
     * @param questionDTOs 原始问题DTO列表
     * @return 导入结果
     */
    DataImportResultDTO importRawQuestions(List<RawQuestionImportDTO> questionDTOs);

    /**
     * 导入单个标准问答对
     * @param qaDTO 标准问答对DTO
     * @return 导入结果
     */
    DataImportResultDTO importStandardQA(StandardQAImportDTO qaDTO);

    /**
     * 批量导入标准问答对
     * @param qaDTOs 标准问答对DTO列表
     * @return 导入结果
     */
    DataImportResultDTO importStandardQAs(List<StandardQAImportDTO> qaDTOs);
    
    /**
     * 获取导入任务历史记录
     * @param type 导入类型
     * @param pageable 分页参数
     * @return 导入任务分页结果
     */
    Page<ImportTaskDTO> getImportHistory(String type, Pageable pageable);
    
    /**
     * 获取导入任务详情
     * @param importId 导入任务ID
     * @return 导入任务DTO
     */
    ImportTaskDTO getImportTaskDetail(Integer importId);
    
    /**
     * 取消导入任务
     * @param importId 导入任务ID
     * @return 更新后的导入任务DTO
     */
    ImportTaskDTO cancelImportTask(Integer importId);
    
    /**
     * 重试失败的导入任务
     * @param importId 导入任务ID
     * @return 新的导入任务DTO
     */
    ImportTaskDTO retryImportTask(Integer importId);
    
    /**
     * 验证原始问答数据格式
     * @param file JSON文件
     * @return 验证结果
     */
    DataImportResultDTO validateRawQA(MultipartFile file) throws IOException;
    
    /**
     * 验证标准问答数据格式
     * @param file JSON文件
     * @return 验证结果
     */
    DataImportResultDTO validateStandardQA(MultipartFile file) throws IOException;

    /**
     * 仅导入单个原始问题（不包含回答）
     * @param questionDTO 原始问题DTO
     * @return 导入结果
     */
    DataImportResultDTO importRawQuestionOnly(RawQuestionOnlyDTO questionDTO);

    /**
     * 批量导入原始问题（不包含回答）
     * @param questionDTOs 原始问题DTO列表
     * @return 导入结果
     */
    DataImportResultDTO importRawQuestionsOnly(List<RawQuestionOnlyDTO> questionDTOs);

    /**
     * 为已存在的原始问题导入一个回答
     * @param answerDTO 原始回答DTO
     * @return 导入结果
     */
    DataImportResultDTO importRawAnswer(RawAnswerDTO answerDTO);

    /**
     * 批量导入原始问题的回答
     * @param answerDTOs 原始回答DTO列表
     * @return 导入结果
     */
    DataImportResultDTO importRawAnswers(List<RawAnswerDTO> answerDTOs);

    /**
     * 仅导入单个标准问题（不包含答案）
     * @param questionDTO 标准问题DTO
     * @return 导入结果
     */
    DataImportResultDTO importStandardQuestion(StandardQuestionDTO questionDTO);

    /**
     * 批量导入标准问题（不包含答案）
     * @param questionDTOs 标准问题DTO列表
     * @return 导入结果
     */
    DataImportResultDTO importStandardQuestions(List<StandardQuestionDTO> questionDTOs);

    /**
     * 为已存在的标准问题导入标准答案
     * @param answerDTO 标准答案DTO
     * @return 导入结果
     */
    DataImportResultDTO importStandardAnswer(StandardAnswerDTO answerDTO);

    /**
     * 批量导入标准答案
     * @param answerDTOs 标准答案DTO列表
     * @return 导入结果
     */
    DataImportResultDTO importStandardAnswers(List<StandardAnswerDTO> answerDTOs);

    /**
     * 创建标准问答对关联
     * @param linkDTO 标准问答关联DTO
     * @return 导入结果
     */
    DataImportResultDTO createStandardQALink(StandardQALinkDTO linkDTO);

    /**
     * 批量创建标准问答对关联
     * @param linkDTOs 标准问答关联DTO列表
     * @return 导入结果
     */
    DataImportResultDTO createStandardQALinks(List<StandardQALinkDTO> linkDTOs);
} 