package com.llm.eval.controller;

import com.llm.eval.dto.DataImportResultDTO;
import com.llm.eval.dto.RawQuestionImportDTO;
import com.llm.eval.dto.StandardQAImportDTO;
import com.llm.eval.dto.RawQuestionOnlyDTO;
import com.llm.eval.dto.RawAnswerDTO;
import com.llm.eval.dto.StandardQuestionDTO;
import com.llm.eval.dto.StandardAnswerDTO;
import com.llm.eval.dto.StandardQALinkDTO;
import com.llm.eval.dto.LlmAnswerDTO;
import com.llm.eval.dto.LlmModelDTO;
import com.llm.eval.dto.DatasetVersionDTO;
import com.llm.eval.service.DataImportService;
import com.llm.eval.service.LlmAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@Tag(name = "Data Import API", description = "数据导入接口")
@Slf4j
public class DataImportController {

    private final DataImportService dataImportService;
    private final LlmAnswerService llmAnswerService;

    @Autowired
    public DataImportController(DataImportService dataImportService, LlmAnswerService llmAnswerService) {
        this.dataImportService = dataImportService;
        this.llmAnswerService = llmAnswerService;
    }

    @PostMapping("/raw-qa")
    @Operation(summary = "导入原始问答数据", description = "上传JSON文件导入原始问答数据")
    public ResponseEntity<DataImportResultDTO> importRawQA(@RequestParam("file") MultipartFile file) {
        if(file.getOriginalFilename().equals("")){
            return ResponseEntity.badRequest().body(
                new DataImportResultDTO(false, "导入失败: 文件名不能为空", 0, 1, 1)
            );
        }
        if(file.getOriginalFilename() != null && file.getOriginalFilename().contains("标准")) {
            return ResponseEntity.badRequest().body(
                new DataImportResultDTO(false, "导入失败: 文件类型不对", 0, 1, 1)
            );
        }
        try {

            log.info("接收到原始问答数据导入请求，文件名：{}", file.getOriginalFilename());
            DataImportResultDTO result = dataImportService.importRawQA(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("导入原始问答数据失败", e);
            return ResponseEntity.badRequest().body(
                    new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1)
            );
        }
    }

    @PostMapping("/standard-qa")
    @Operation(summary = "导入标准问答数据", description = "上传JSON文件导入标准问答数据")
    public ResponseEntity<DataImportResultDTO> importStandardQA(@RequestParam("file") MultipartFile file) {
        if(file.getOriginalFilename() != null && file.getOriginalFilename().contains("原始")) {
            return ResponseEntity.badRequest().body(
                new DataImportResultDTO(false, "导入失败: 文件类型不对", 0, 1, 1)
            );
        }
        
        try {
            log.info("接收到标准问答数据导入请求，文件名：{}", file.getOriginalFilename());
            DataImportResultDTO result = dataImportService.importStandardQA(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("导入标准问答数据失败", e);
            return ResponseEntity.badRequest().body(
                    new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1)
            );
        }
    }

    @PostMapping("/raw-question")
    @Operation(summary = "导入单个原始问题", description = "导入单个原始问题及其答案")
    public ResponseEntity<DataImportResultDTO> importRawQuestion(@RequestBody RawQuestionImportDTO questionDTO) {
        log.info("接收到单个原始问题导入请求");
        DataImportResultDTO result = dataImportService.importRawQuestion(questionDTO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/raw-questions")
    @Operation(summary = "批量导入原始问题", description = "批量导入原始问题及其答案")
    public ResponseEntity<DataImportResultDTO> importRawQuestions(@RequestBody List<RawQuestionImportDTO> questionDTOs) {
        log.info("接收到批量原始问题导入请求，数量：{}", questionDTOs.size());
        DataImportResultDTO result = dataImportService.importRawQuestions(questionDTOs);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/standard-qa-single")
    @Operation(summary = "导入单个标准问答对", description = "导入单个标准问题、答案及关键点")
    public ResponseEntity<DataImportResultDTO> importStandardQA(@RequestBody StandardQAImportDTO qaDTO) {
        log.info("接收到单个标准问答对导入请求");
        DataImportResultDTO result = dataImportService.importStandardQA(qaDTO);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/standard-qas")
    @Operation(summary = "批量导入标准问答对", description = "批量导入标准问题、答案及关键点")
    public ResponseEntity<DataImportResultDTO> importStandardQAs(@RequestBody List<StandardQAImportDTO> qaDTOs) {
        log.info("接收到批量标准问答对导入请求，数量：{}", qaDTOs.size());
        DataImportResultDTO result = dataImportService.importStandardQAs(qaDTOs);
        return ResponseEntity.ok(result);
    }
    
    // 新增的API接口方法
    @PostMapping("/raw-question-only")
    @Operation(summary = "仅导入单个原始问题", description = "导入单个原始问题，不包含回答")
    public ResponseEntity<DataImportResultDTO> importRawQuestionOnly(@RequestBody RawQuestionOnlyDTO questionDTO) {
        log.info("接收到单个原始问题导入请求（不含回答）");
        DataImportResultDTO result = dataImportService.importRawQuestionOnly(questionDTO);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/raw-questions-only")
    @Operation(summary = "批量导入原始问题", description = "批量导入原始问题，不包含回答")
    public ResponseEntity<DataImportResultDTO> importRawQuestionsOnly(@RequestBody List<RawQuestionOnlyDTO> questionDTOs) {
        log.info("接收到批量原始问题导入请求（不含回答），数量：{}", questionDTOs.size());
        DataImportResultDTO result = dataImportService.importRawQuestionsOnly(questionDTOs);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/raw-answer")
    @Operation(summary = "导入单个原始回答", description = "为已存在的原始问题导入一个回答")
    public ResponseEntity<DataImportResultDTO> importRawAnswer(@RequestBody RawAnswerDTO answerDTO) {
        log.info("接收到单个原始回答导入请求，问题ID：{}", answerDTO.getQuestionId());
        DataImportResultDTO result = dataImportService.importRawAnswer(answerDTO);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/raw-answers")
    @Operation(summary = "批量导入原始回答", description = "批量导入多个原始问题的回答")
    public ResponseEntity<DataImportResultDTO> importRawAnswers(@RequestBody List<RawAnswerDTO> answerDTOs) {
        log.info("接收到批量原始回答导入请求，数量：{}", answerDTOs.size());
        DataImportResultDTO result = dataImportService.importRawAnswers(answerDTOs);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/standard-question")
    @Operation(summary = "导入单个标准问题", description = "导入单个标准问题，不包含答案")
    public ResponseEntity<DataImportResultDTO> importStandardQuestion(@RequestBody StandardQuestionDTO questionDTO) {
        log.info("接收到单个标准问题导入请求");
        DataImportResultDTO result = dataImportService.importStandardQuestion(questionDTO);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/standard-questions")
    @Operation(summary = "批量导入标准问题", description = "批量导入标准问题，不包含答案")
    public ResponseEntity<DataImportResultDTO> importStandardQuestions(@RequestBody List<StandardQuestionDTO> questionDTOs) {
        log.info("接收到批量标准问题导入请求，数量：{}", questionDTOs.size());
        DataImportResultDTO result = dataImportService.importStandardQuestions(questionDTOs);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/standard-answer")
    @Operation(summary = "导入单个标准答案", description = "为已存在的标准问题导入标准答案")
    public ResponseEntity<DataImportResultDTO> importStandardAnswer(@RequestBody StandardAnswerDTO answerDTO) {
        log.info("接收到单个标准答案导入请求，问题ID：{}", answerDTO.getStandardQuestionId());
        DataImportResultDTO result = dataImportService.importStandardAnswer(answerDTO);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/standard-answers")
    @Operation(summary = "批量导入标准答案", description = "批量导入多个标准问题的标准答案")
    public ResponseEntity<DataImportResultDTO> importStandardAnswers(@RequestBody List<StandardAnswerDTO> answerDTOs) {
        log.info("接收到批量标准答案导入请求，数量：{}", answerDTOs.size());
        DataImportResultDTO result = dataImportService.importStandardAnswers(answerDTOs);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/standard-qa-link")
    @Operation(summary = "创建标准问答对关联", description = "创建标准问题和标准答案的关联关系")
    public ResponseEntity<DataImportResultDTO> createStandardQALink(@RequestBody StandardQALinkDTO linkDTO) {
        log.info("接收到标准问答对关联创建请求");
        DataImportResultDTO result = dataImportService.createStandardQALink(linkDTO);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/standard-qa-links")
    @Operation(summary = "批量创建标准问答对关联", description = "批量创建标准问题和标准答案的关联关系")
    public ResponseEntity<DataImportResultDTO> createStandardQALinks(@RequestBody List<StandardQALinkDTO> linkDTOs) {
        log.info("接收到批量标准问答对关联创建请求，数量：{}", linkDTOs.size());
        DataImportResultDTO result = dataImportService.createStandardQALinks(linkDTOs);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/raw-questions-only-file")
    @Operation(summary = "文件上传导入原始问题", description = "通过文件上传批量导入原始问题，不包含回答")
    public ResponseEntity<DataImportResultDTO> importRawQuestionsOnlyFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("接收到文件上传导入原始问题请求（不含回答），文件名：{}", file.getOriginalFilename());
            
            ObjectMapper objectMapper = new ObjectMapper();
            List<RawQuestionOnlyDTO> questionDTOs = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<RawQuestionOnlyDTO>>() {}
            );
            
            DataImportResultDTO result = dataImportService.importRawQuestionsOnly(questionDTOs);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("文件上传导入原始问题失败", e);
            return ResponseEntity.badRequest().body(
                    new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1)
            );
        }
    }
    
    @PostMapping("/raw-answers-file")
    @Operation(summary = "文件上传导入原始回答", description = "通过文件上传批量导入原始回答")
    public ResponseEntity<DataImportResultDTO> importRawAnswersFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("接收到文件上传导入原始回答请求，文件名：{}", file.getOriginalFilename());
            
            ObjectMapper objectMapper = new ObjectMapper();
            List<RawAnswerDTO> answerDTOs = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<RawAnswerDTO>>() {}
            );
            
            DataImportResultDTO result = dataImportService.importRawAnswers(answerDTOs);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("文件上传导入原始回答失败", e);
            return ResponseEntity.badRequest().body(
                    new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1)
            );
        }
    }

    @PostMapping("/model-answer")
    @Operation(summary = "导入单个模型回答", description = "导入单个模型对标准问题的回答")
    public ResponseEntity<DataImportResultDTO> importModelAnswer(@RequestBody Map<String, Object> requestData) {
        log.info("接收到模型回答导入请求: {}", requestData);
        try {
            // 从请求中提取数据
            Integer questionId = (Integer) requestData.get("questionId");
            Integer modelId = (Integer) requestData.get("modelId");
            String answer = (String) requestData.get("answer");
            Integer responseTime = (Integer) requestData.get("responseTime");
            String status = (String) requestData.get("status");
            Integer datasetId = (Integer) requestData.get("datasetId");
            
            if (questionId == null || modelId == null || answer == null) {
                return ResponseEntity.badRequest().body(
                    new DataImportResultDTO(false, "导入失败: 问题ID、模型ID和回答内容不能为空", 0, 0, 1)
                );
            }
            
            // 创建LlmAnswerDTO对象
            LlmAnswerDTO answerDTO = new LlmAnswerDTO();
            // 设置问题ID - 注意这里需要通过DTO中的standardQuestion对象设置
            StandardQuestionDTO questionDTO = new StandardQuestionDTO();
            questionDTO.setStandardQuestionId(questionId);
            answerDTO.setStandardQuestion(questionDTO);
            
            // 设置模型ID - 注意这里需要通过DTO中的model对象设置
            LlmModelDTO modelDTO = new LlmModelDTO();
            modelDTO.setModelId(modelId);
            answerDTO.setModel(modelDTO);
            
            // 如果提供了数据集ID，则设置数据集信息
            if (datasetId != null) {
                DatasetVersionDTO datasetDTO = new DatasetVersionDTO();
                datasetDTO.setVersionId(datasetId);
                answerDTO.setDatasetVersion(datasetDTO);
            }
            
            // 设置其他属性
            answerDTO.setContent(answer);
            answerDTO.setLatency(responseTime);
            
            // 保存到数据库
            LlmAnswerDTO savedAnswer = llmAnswerService.createAnswer(answerDTO);
            
            if (savedAnswer != null && savedAnswer.getLlmAnswerId() != null) {
                return ResponseEntity.ok(
                    new DataImportResultDTO(true, "模型回答导入成功", 1, 1, 0)
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new DataImportResultDTO(false, "导入失败: 保存模型回答时发生错误", 0, 0, 1)
                );
            }
        } catch (Exception e) {
            log.error("导入模型回答失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 0, 1)
            );
        }
    }
} 