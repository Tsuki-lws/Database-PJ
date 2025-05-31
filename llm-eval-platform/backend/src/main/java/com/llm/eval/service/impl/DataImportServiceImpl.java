package com.llm.eval.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.llm.eval.dto.DataImportResultDTO;
import com.llm.eval.dto.ImportTaskDTO;
import com.llm.eval.dto.RawQuestionImportDTO;
import com.llm.eval.dto.StandardQAImportDTO;
import com.llm.eval.dto.RawAnswerImportDTO;
import com.llm.eval.dto.RawQuestionOnlyDTO;
import com.llm.eval.dto.RawAnswerDTO;
import com.llm.eval.dto.StandardQuestionDTO;
import com.llm.eval.dto.StandardAnswerDTO;
import com.llm.eval.dto.StandardQALinkDTO;
import com.llm.eval.model.*;
import com.llm.eval.repository.*;
import com.llm.eval.service.DataImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataImportServiceImpl implements DataImportService {

    private final ObjectMapper objectMapper;
    private final RawQuestionRepository rawQuestionRepository;
    private final RawAnswerRepository rawAnswerRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    private final StandardAnswerRepository standardAnswerRepository;
    private final AnswerKeyPointRepository keyPointRepository;
    private final StandardQAPairRepository standardQAPairRepository;
    private final QuestionCategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Autowired
    public DataImportServiceImpl(
            ObjectMapper objectMapper,
            RawQuestionRepository rawQuestionRepository,
            RawAnswerRepository rawAnswerRepository,
            StandardQuestionRepository standardQuestionRepository,
            StandardAnswerRepository standardAnswerRepository,
            AnswerKeyPointRepository keyPointRepository,
            StandardQAPairRepository standardQAPairRepository,
            QuestionCategoryRepository categoryRepository,
            TagRepository tagRepository) {
        this.objectMapper = objectMapper;
        this.rawQuestionRepository = rawQuestionRepository;
        this.rawAnswerRepository = rawAnswerRepository;
        this.standardQuestionRepository = standardQuestionRepository;
        this.standardAnswerRepository = standardAnswerRepository;
        this.keyPointRepository = keyPointRepository;
        this.standardQAPairRepository = standardQAPairRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public DataImportResultDTO importRawQA(MultipartFile file) throws IOException {
        log.info("开始导入原始问答数据，文件名: {}", file.getOriginalFilename());
        
        List<Map<String, Object>> rawQAs = objectMapper.readValue(
                file.getInputStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
        );
        
        int total = rawQAs.size();
        int imported = 0;
        int failed = 0;
        
        for (Map<String, Object> rawQA : rawQAs) {
            try {
                RawQuestionImportDTO questionDTO = new RawQuestionImportDTO();
                questionDTO.setSourceQuestionId((Integer) rawQA.get("source_question_id"));
                questionDTO.setQuestionTitle((String) rawQA.get("question_title"));
                questionDTO.setQuestionBody((String) rawQA.get("question_body"));
                questionDTO.setSource((String) rawQA.get("source"));
                questionDTO.setSourceId((Integer) rawQA.get("source_id"));
                questionDTO.setSourceUrl((String) rawQA.get("source_url"));
                
                RawAnswerImportDTO answerDTO = new RawAnswerImportDTO();
                answerDTO.setSourceAnswerId((Integer) rawQA.get("source_answer_id"));
                answerDTO.setAnswerBody((String) rawQA.get("answer_body"));
                
                questionDTO.setAnswer(answerDTO);
                
                importRawQuestion(questionDTO);
                imported++;
            } catch (Exception e) {
                log.error("导入原始问答数据失败", e);
                failed++;
            }
        }
        
        return new DataImportResultDTO(true, "导入完成", imported, failed, total);
    }

    @Override
    @Transactional
    public DataImportResultDTO importStandardQA(MultipartFile file) throws IOException {
        log.info("开始导入标准问答数据，文件名: {}", file.getOriginalFilename());
        
        List<StandardQAImportDTO> standardQAs = objectMapper.readValue(
                file.getInputStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, StandardQAImportDTO.class)
        );
        
        return importStandardQAs(standardQAs);
    }

    @Override
    @Transactional
    public DataImportResultDTO importRawQuestion(RawQuestionImportDTO questionDTO) {
        log.info("导入单个原始问题: {}", questionDTO.getQuestionTitle());
        
        try {
            // 检查问题是否已存在
            Optional<RawQuestion> existingQuestion = rawQuestionRepository.findBySourceQuestionId(questionDTO.getSourceQuestionId());
            
            RawQuestion rawQuestion;
            if (existingQuestion.isPresent()) {
                // 更新已有问题
                rawQuestion = existingQuestion.get();
                rawQuestion.setQuestionTitle(questionDTO.getQuestionTitle());
                rawQuestion.setQuestionBody(questionDTO.getQuestionBody());
                rawQuestion.setSource(questionDTO.getSource());
                rawQuestion.setSourceId(questionDTO.getSourceId());
                rawQuestion.setSourceUrl(questionDTO.getSourceUrl());
                rawQuestion.setUpdatedAt(LocalDateTime.now());
            } else {
                // 创建新问题
                rawQuestion = new RawQuestion();
                rawQuestion.setSourceQuestionId(questionDTO.getSourceQuestionId());
                rawQuestion.setQuestionTitle(questionDTO.getQuestionTitle());
                rawQuestion.setQuestionBody(questionDTO.getQuestionBody());
                rawQuestion.setSource(questionDTO.getSource());
                rawQuestion.setSourceId(questionDTO.getSourceId());
                rawQuestion.setSourceUrl(questionDTO.getSourceUrl());
                rawQuestion.setCreatedAt(LocalDateTime.now());
                rawQuestion.setUpdatedAt(LocalDateTime.now());
            }
            
            // 保存问题
            rawQuestion = rawQuestionRepository.save(rawQuestion);
            
            // 如果有答案，保存答案
            if (questionDTO.getAnswer() != null) {
                RawAnswerImportDTO answerDTO = questionDTO.getAnswer();
                
                // 检查答案是否已存在
                Optional<RawAnswer> existingAnswer = rawAnswerRepository.findBySourceAnswerId(answerDTO.getSourceAnswerId());
                
                RawAnswer rawAnswer;
                if (existingAnswer.isPresent()) {
                    // 更新已有答案
                    rawAnswer = existingAnswer.get();
                    rawAnswer.setAnswerBody(answerDTO.getAnswerBody());
                    rawAnswer.setAuthorInfo(answerDTO.getAuthorInfo());
                    rawAnswer.setUpvotes(answerDTO.getUpvotes());
                    rawAnswer.setIsAccepted(answerDTO.getIsAccepted());
                    rawAnswer.setUpdatedAt(LocalDateTime.now());
                } else {
                    // 创建新答案
                    rawAnswer = new RawAnswer();
                    rawAnswer.setSourceAnswerId(answerDTO.getSourceAnswerId());
                    rawAnswer.setQuestion(rawQuestion);
                    rawAnswer.setAnswerBody(answerDTO.getAnswerBody());
                    rawAnswer.setAuthorInfo(answerDTO.getAuthorInfo());
                    rawAnswer.setUpvotes(answerDTO.getUpvotes());
                    rawAnswer.setIsAccepted(answerDTO.getIsAccepted());
                    rawAnswer.setCreatedAt(LocalDateTime.now());
                    rawAnswer.setUpdatedAt(LocalDateTime.now());
                }
                
                // 保存答案
                rawAnswerRepository.save(rawAnswer);
            }
            
            return new DataImportResultDTO(true, "导入成功", 1, 0, 1);
        } catch (Exception e) {
            log.error("导入原始问题失败", e);
            return new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    @Transactional
    public DataImportResultDTO importRawQuestions(List<RawQuestionImportDTO> questionDTOs) {
        log.info("批量导入原始问题，数量: {}", questionDTOs.size());
        
        int total = questionDTOs.size();
        int imported = 0;
        int failed = 0;
        
        for (RawQuestionImportDTO questionDTO : questionDTOs) {
            DataImportResultDTO result = importRawQuestion(questionDTO);
            if (result.isSuccess()) {
                imported++;
            } else {
                failed++;
            }
        }
        
        return new DataImportResultDTO(failed == 0, "导入完成", imported, failed, total);
    }

    @Override
    @Transactional
    public DataImportResultDTO importStandardQA(StandardQAImportDTO qaDTO) {
        log.info("导入单个标准问答对: {}", qaDTO.getQuestion());
        
        try {
            // 查找原始问题和答案
            Optional<RawQuestion> rawQuestionOpt = rawQuestionRepository.findBySourceQuestionId(qaDTO.getSourceQuestionId());
            Optional<RawAnswer> rawAnswerOpt = rawAnswerRepository.findBySourceAnswerId(qaDTO.getSourceAnswerId());
            
            // 检查标准问题是否已存在
            Optional<StandardQuestion> existingQuestionOpt = standardQuestionRepository.findByQuestionAndSourceQuestionId(
                    qaDTO.getQuestion(), 
                    rawQuestionOpt.isPresent() ? rawQuestionOpt.get().getQuestionId() : null
            );
            
            StandardQuestion standardQuestion;
            if (existingQuestionOpt.isPresent()) {
                // 更新已有标准问题
                standardQuestion = existingQuestionOpt.get();
                standardQuestion.setQuestion(qaDTO.getQuestion());
                standardQuestion.setUpdatedAt(LocalDateTime.now());
            } else {
                // 创建新标准问题
                standardQuestion = new StandardQuestion();
                standardQuestion.setQuestion(qaDTO.getQuestion());
                standardQuestion.setQuestionType(StandardQuestion.QuestionType.subjective);
                standardQuestion.setDifficulty(StandardQuestion.DifficultyLevel.medium);
                standardQuestion.setStatus(StandardQuestion.QuestionStatus.approved);
                standardQuestion.setCreatedAt(LocalDateTime.now());
                standardQuestion.setUpdatedAt(LocalDateTime.now());
                standardQuestion.setVersion(1);
                
                // 设置源问题
                if (rawQuestionOpt.isPresent()) {
                    standardQuestion.setSourceQuestion(rawQuestionOpt.get());
                }
            }
            
            // 保存标准问题
            standardQuestion = standardQuestionRepository.save(standardQuestion);
            
            // 创建标准答案
            StandardAnswer standardAnswer = new StandardAnswer();
            standardAnswer.setStandardQuestion(standardQuestion);
            standardAnswer.setAnswer(qaDTO.getAnswer());
            
            // 设置源答案
            if (rawAnswerOpt.isPresent()) {
                standardAnswer.setSourceAnswer(rawAnswerOpt.get());
                standardAnswer.setSourceType(StandardAnswer.SourceType.raw);
            } else {
                standardAnswer.setSourceType(StandardAnswer.SourceType.manual);
            }
            
            standardAnswer.setIsFinal(true);
            standardAnswer.setCreatedAt(LocalDateTime.now());
            standardAnswer.setUpdatedAt(LocalDateTime.now());
            standardAnswer.setVersion(1);
            
            // 保存标准答案
            standardAnswer = standardAnswerRepository.save(standardAnswer);
            
            // 创建关键点
            if (qaDTO.getKeyPoints() != null && !qaDTO.getKeyPoints().isEmpty()) {
                List<AnswerKeyPoint> keyPoints = new ArrayList<>();
                
                for (int i = 0; i < qaDTO.getKeyPoints().size(); i++) {
                    String keyPointText = qaDTO.getKeyPoints().get(i);
                    
                    AnswerKeyPoint keyPoint = new AnswerKeyPoint();
                    keyPoint.setStandardAnswer(standardAnswer);
                    keyPoint.setPointText(keyPointText);
                    keyPoint.setPointOrder(i + 1);
                    keyPoint.setPointWeight(BigDecimal.ONE);
                    keyPoint.setPointType(AnswerKeyPoint.PointType.required);
                    keyPoint.setCreatedAt(LocalDateTime.now());
                    keyPoint.setUpdatedAt(LocalDateTime.now());
                    keyPoint.setVersion(1);
                    
                    keyPoints.add(keyPoint);
                }
                
                // 批量保存关键点
                keyPointRepository.saveAll(keyPoints);
            }
            
            return new DataImportResultDTO(true, "导入成功", 1, 0, 1);
        } catch (Exception e) {
            log.error("导入标准问答对失败", e);
            return new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    @Transactional
    public DataImportResultDTO importStandardQAs(List<StandardQAImportDTO> qaDTOs) {
        log.info("批量导入标准问答对，数量: {}", qaDTOs.size());
        
        int total = qaDTOs.size();
        int imported = 0;
        int failed = 0;
        
        for (StandardQAImportDTO qaDTO : qaDTOs) {
            DataImportResultDTO result = importStandardQA(qaDTO);
            if (result.isSuccess()) {
                imported++;
            } else {
                failed++;
            }
        }
        
        return new DataImportResultDTO(failed == 0, "导入完成", imported, failed, total);
    }

    @Override
    public DataImportResultDTO validateStandardQA(MultipartFile file) throws IOException {
        log.info("验证标准问答数据格式，文件名: {}", file.getOriginalFilename());
        
        try {
            List<StandardQAImportDTO> standardQAs = objectMapper.readValue(
                    file.getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, StandardQAImportDTO.class)
            );
            
            int total = standardQAs.size();
            
            if (total == 0) {
                return new DataImportResultDTO(false, "文件内容为空", 0, 0, 0);
            }
            
            // 验证每一个标准问答对
            for (StandardQAImportDTO qa : standardQAs) {
                if (qa.getQuestion() == null || qa.getQuestion().trim().isEmpty()) {
                    return new DataImportResultDTO(false, "问题内容不能为空", 0, 1, total);
                }
                
                if (qa.getAnswer() == null || qa.getAnswer().trim().isEmpty()) {
                    return new DataImportResultDTO(false, "答案内容不能为空", 0, 1, total);
                }
            }
            
            return new DataImportResultDTO(true, "验证通过", total, 0, total);
        } catch (Exception e) {
            log.error("验证标准问答数据格式失败", e);
            return new DataImportResultDTO(false, "验证失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    public DataImportResultDTO validateRawQA(MultipartFile file) throws IOException {
        log.info("验证原始问答数据格式，文件名: {}", file.getOriginalFilename());
        
        try {
            List<Map<String, Object>> rawQAs = objectMapper.readValue(
                    file.getInputStream(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            
            int total = rawQAs.size();
            
            if (total == 0) {
                return new DataImportResultDTO(false, "文件内容为空", 0, 0, 0);
            }
            
            // 验证每一个原始问答对
            for (Map<String, Object> rawQA : rawQAs) {
                // 验证必要字段
                if (!rawQA.containsKey("source_question_id") || !rawQA.containsKey("question_title") || !rawQA.containsKey("question_body")) {
                    return new DataImportResultDTO(false, "原始问题缺少必要字段", 0, 1, total);
                }
                
                if (!rawQA.containsKey("source_answer_id") || !rawQA.containsKey("answer_body")) {
                    return new DataImportResultDTO(false, "原始答案缺少必要字段", 0, 1, total);
                }
            }
            
            return new DataImportResultDTO(true, "验证通过", total, 0, total);
        } catch (Exception e) {
            log.error("验证原始问答数据格式失败", e);
            return new DataImportResultDTO(false, "验证失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    public Page<ImportTaskDTO> getImportHistory(String type, Pageable pageable) {
        // 暂时返回空结果，具体实现根据业务需求添加
        return Page.empty();
    }
    
    @Override
    public ImportTaskDTO getImportTaskDetail(Integer importId) {
        // 暂时返回null，具体实现根据业务需求添加
        return null;
    }
    
    @Override
    public ImportTaskDTO cancelImportTask(Integer importId) {
        // 暂时返回null，具体实现根据业务需求添加
        return null;
    }
    
    @Override
    public ImportTaskDTO retryImportTask(Integer importId) {
        // 暂时返回null，具体实现根据业务需求添加
        return null;
    }
    
    @Override
    @Transactional
    public DataImportResultDTO importRawQuestionOnly(RawQuestionOnlyDTO questionDTO) {
        log.info("导入单个原始问题（不含回答）: {}", questionDTO.getQuestionTitle());
        
        try {
            // 检查问题是否已存在
            Optional<RawQuestion> existingQuestion = rawQuestionRepository.findBySourceQuestionId(questionDTO.getSourceQuestionId());
            
            RawQuestion rawQuestion;
            if (existingQuestion.isPresent()) {
                // 更新已有问题
                rawQuestion = existingQuestion.get();
                rawQuestion.setQuestionTitle(questionDTO.getQuestionTitle());
                rawQuestion.setQuestionBody(questionDTO.getQuestionBody());
                rawQuestion.setSource(questionDTO.getSource());
                rawQuestion.setSourceId(questionDTO.getSourceId());
                rawQuestion.setSourceUrl(questionDTO.getSourceUrl());
                rawQuestion.setUpdatedAt(LocalDateTime.now());
            } else {
                // 创建新问题
                rawQuestion = new RawQuestion();
                rawQuestion.setSourceQuestionId(questionDTO.getSourceQuestionId());
                rawQuestion.setQuestionTitle(questionDTO.getQuestionTitle());
                rawQuestion.setQuestionBody(questionDTO.getQuestionBody());
                rawQuestion.setSource(questionDTO.getSource());
                rawQuestion.setSourceId(questionDTO.getSourceId());
                rawQuestion.setSourceUrl(questionDTO.getSourceUrl());
                rawQuestion.setCreatedAt(LocalDateTime.now());
                rawQuestion.setUpdatedAt(LocalDateTime.now());
            }
            
            // 保存问题
            rawQuestionRepository.save(rawQuestion);
            
            return new DataImportResultDTO(true, "原始问题导入成功", 1, 0, 1);
        } catch (Exception e) {
            log.error("导入原始问题失败", e);
            return new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    @Transactional
    public DataImportResultDTO importRawQuestionsOnly(List<RawQuestionOnlyDTO> questionDTOs) {
        log.info("批量导入原始问题（不含回答），数量: {}", questionDTOs.size());
        
        int total = questionDTOs.size();
        int imported = 0;
        int failed = 0;
        
        for (RawQuestionOnlyDTO questionDTO : questionDTOs) {
            DataImportResultDTO result = importRawQuestionOnly(questionDTO);
            if (result.isSuccess()) {
                imported++;
            } else {
                failed++;
            }
        }
        
        return new DataImportResultDTO(failed == 0, "批量导入原始问题完成", imported, failed, total);
    }

    @Override
    @Transactional
    public DataImportResultDTO importRawAnswer(RawAnswerDTO answerDTO) {
        log.info("为原始问题导入回答，问题ID: {}", answerDTO.getQuestionId());
        
        try {
            // 检查问题是否存在
            Optional<RawQuestion> questionOpt = rawQuestionRepository.findById(answerDTO.getQuestionId());
            if (!questionOpt.isPresent()) {
                return new DataImportResultDTO(false, "导入失败: 原始问题不存在", 0, 1, 1);
            }
            
            RawQuestion rawQuestion = questionOpt.get();
            
            // 检查答案是否已存在
            Optional<RawAnswer> existingAnswer = rawAnswerRepository.findBySourceAnswerId(answerDTO.getSourceAnswerId());
            
            RawAnswer rawAnswer;
            if (existingAnswer.isPresent()) {
                // 更新已有答案
                rawAnswer = existingAnswer.get();
                rawAnswer.setAnswerBody(answerDTO.getAnswerBody());
                rawAnswer.setAuthorInfo(answerDTO.getAuthorInfo());
                rawAnswer.setUpvotes(answerDTO.getUpvotes());
                rawAnswer.setIsAccepted(answerDTO.getIsAccepted());
                rawAnswer.setUpdatedAt(LocalDateTime.now());
            } else {
                // 创建新答案
                rawAnswer = new RawAnswer();
                rawAnswer.setSourceAnswerId(answerDTO.getSourceAnswerId());
                rawAnswer.setQuestion(rawQuestion);
                rawAnswer.setAnswerBody(answerDTO.getAnswerBody());
                rawAnswer.setAuthorInfo(answerDTO.getAuthorInfo());
                rawAnswer.setUpvotes(answerDTO.getUpvotes());
                rawAnswer.setIsAccepted(answerDTO.getIsAccepted());
                rawAnswer.setCreatedAt(LocalDateTime.now());
                rawAnswer.setUpdatedAt(LocalDateTime.now());
            }
            
            // 保存答案
            rawAnswerRepository.save(rawAnswer);
            
            return new DataImportResultDTO(true, "原始回答导入成功", 1, 0, 1);
        } catch (Exception e) {
            log.error("导入原始回答失败", e);
            return new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    @Transactional
    public DataImportResultDTO importRawAnswers(List<RawAnswerDTO> answerDTOs) {
        log.info("批量导入原始回答，数量: {}", answerDTOs.size());
        
        int total = answerDTOs.size();
        int imported = 0;
        int failed = 0;
        
        for (RawAnswerDTO answerDTO : answerDTOs) {
            DataImportResultDTO result = importRawAnswer(answerDTO);
            if (result.isSuccess()) {
                imported++;
            } else {
                failed++;
            }
        }
        
        return new DataImportResultDTO(failed == 0, "批量导入原始回答完成", imported, failed, total);
    }

    @Override
    @Transactional
    public DataImportResultDTO importStandardQuestion(StandardQuestionDTO questionDTO) {
        log.info("导入单个标准问题: {}", questionDTO.getQuestion());
        
        try {
            // 查找原始问题
            RawQuestion rawQuestion = null;
            if (questionDTO.getSourceQuestionId() != null) {
                Optional<RawQuestion> rawQuestionOpt = rawQuestionRepository.findById(questionDTO.getSourceQuestionId());
                if (rawQuestionOpt.isPresent()) {
                    rawQuestion = rawQuestionOpt.get();
                }
            }
            
            // 查找分类
            QuestionCategory category = null;
            if (questionDTO.getCategoryId() != null) {
                Optional<QuestionCategory> categoryOpt = categoryRepository.findById(questionDTO.getCategoryId());
                if (categoryOpt.isPresent()) {
                    category = categoryOpt.get();
                }
            }
            
            // 检查标准问题是否已存在
            Optional<StandardQuestion> existingQuestionOpt = standardQuestionRepository.findByQuestionAndSourceQuestionId(
                    questionDTO.getQuestion(), 
                    rawQuestion != null ? rawQuestion.getQuestionId() : null
            );
            
            StandardQuestion standardQuestion;
            if (existingQuestionOpt.isPresent()) {
                // 更新已有标准问题
                standardQuestion = existingQuestionOpt.get();
                standardQuestion.setQuestion(questionDTO.getQuestion());
                standardQuestion.setCategory(category);
                standardQuestion.setQuestionType(StandardQuestion.QuestionType.valueOf(questionDTO.getQuestionType()));
                standardQuestion.setDifficulty(StandardQuestion.DifficultyLevel.valueOf(questionDTO.getDifficulty()));
                standardQuestion.setUpdatedAt(LocalDateTime.now());
            } else {
                // 创建新标准问题
                standardQuestion = new StandardQuestion();
                standardQuestion.setQuestion(questionDTO.getQuestion());
                standardQuestion.setCategory(category);
                standardQuestion.setQuestionType(StandardQuestion.QuestionType.valueOf(questionDTO.getQuestionType()));
                standardQuestion.setDifficulty(StandardQuestion.DifficultyLevel.valueOf(questionDTO.getDifficulty()));
                standardQuestion.setStatus(StandardQuestion.QuestionStatus.draft);
                standardQuestion.setSourceQuestion(rawQuestion);
                standardQuestion.setCreatedAt(LocalDateTime.now());
                standardQuestion.setUpdatedAt(LocalDateTime.now());
                standardQuestion.setVersion(1);
            }
            
            // 保存标准问题
            standardQuestion = standardQuestionRepository.save(standardQuestion);
            
            // 设置标签
            if (questionDTO.getTags() != null && !questionDTO.getTags().isEmpty()) {
                Set<Tag> tags = new HashSet<>();
                for (Integer tagId : questionDTO.getTags()) {
                    Optional<Tag> tagOpt = tagRepository.findById(tagId);
                    if (tagOpt.isPresent()) {
                        tags.add(tagOpt.get());
                    }
                }
                standardQuestion.setTags(tags);
                standardQuestionRepository.save(standardQuestion);
            }
            
            return new DataImportResultDTO(true, "标准问题导入成功", 1, 0, 1);
        } catch (Exception e) {
            log.error("导入标准问题失败", e);
            return new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    @Transactional
    public DataImportResultDTO importStandardQuestions(List<StandardQuestionDTO> questionDTOs) {
        log.info("批量导入标准问题，数量: {}", questionDTOs.size());
        
        int total = questionDTOs.size();
        int imported = 0;
        int failed = 0;
        
        for (StandardQuestionDTO questionDTO : questionDTOs) {
            DataImportResultDTO result = importStandardQuestion(questionDTO);
            if (result.isSuccess()) {
                imported++;
            } else {
                failed++;
            }
        }
        
        return new DataImportResultDTO(failed == 0, "批量导入标准问题完成", imported, failed, total);
    }

    @Override
    @Transactional
    public DataImportResultDTO importStandardAnswer(StandardAnswerDTO answerDTO) {
        log.info("为标准问题导入标准答案，问题ID: {}", answerDTO.getStandardQuestionId());
        
        try {
            // 检查标准问题是否存在
            Optional<StandardQuestion> questionOpt = standardQuestionRepository.findById(answerDTO.getStandardQuestionId());
            if (!questionOpt.isPresent()) {
                return new DataImportResultDTO(false, "导入失败: 标准问题不存在", 0, 1, 1);
            }
            
            StandardQuestion standardQuestion = questionOpt.get();
            
            // 查找原始回答
            RawAnswer rawAnswer = null;
            if (answerDTO.getSourceAnswerId() != null) {
                Optional<RawAnswer> rawAnswerOpt = rawAnswerRepository.findById(answerDTO.getSourceAnswerId());
                if (rawAnswerOpt.isPresent()) {
                    rawAnswer = rawAnswerOpt.get();
                }
            }
            
            // 创建标准答案
            StandardAnswer standardAnswer = new StandardAnswer();
            standardAnswer.setStandardQuestion(standardQuestion);
            standardAnswer.setAnswer(answerDTO.getAnswer());
            standardAnswer.setSourceAnswer(rawAnswer);
            standardAnswer.setSourceType(StandardAnswer.SourceType.valueOf(answerDTO.getSourceType()));
            standardAnswer.setIsFinal(true); // 默认设置为最终答案
            standardAnswer.setCreatedAt(LocalDateTime.now());
            standardAnswer.setUpdatedAt(LocalDateTime.now());
            standardAnswer.setVersion(1);
            
            // 保存标准答案
            standardAnswer = standardAnswerRepository.save(standardAnswer);
            
            // 添加关键点
            if (answerDTO.getKeyPoints() != null && !answerDTO.getKeyPoints().isEmpty()) {
                log.info("开始处理关键点，数量: {}", answerDTO.getKeyPoints().size());
                int pointOrder = 1; // 添加一个索引变量来跟踪顺序
                for (StandardAnswerDTO.KeyPointDTO keyPointDTO : answerDTO.getKeyPoints()) {
                    log.info("处理关键点: {}, 权重: {}, 类型: {}", 
                             keyPointDTO.getPointText(), 
                             keyPointDTO.getPointWeight(),
                             keyPointDTO.getPointType());
                    
                    AnswerKeyPoint keyPoint = new AnswerKeyPoint();
                    keyPoint.setStandardAnswer(standardAnswer);
                    keyPoint.setPointText(keyPointDTO.getPointText());
                    keyPoint.setPointOrder(pointOrder++); // 设置pointOrder并递增
                    log.info("设置pointOrder: {}", pointOrder-1);
                    
                    // 设置权重，如果为null则设置默认值
                    if (keyPointDTO.getPointWeight() != null) {
                        keyPoint.setPointWeight(keyPointDTO.getPointWeight());
                    } else {
                        keyPoint.setPointWeight(BigDecimal.ONE);
                        log.warn("关键点权重为null，设置默认值1.0");
                    }
                    
                    // 设置类型，如果为null则设置默认值
                    if (keyPointDTO.getPointType() != null) {
                        keyPoint.setPointType(AnswerKeyPoint.PointType.valueOf(keyPointDTO.getPointType()));
                    } else {
                        keyPoint.setPointType(AnswerKeyPoint.PointType.required);
                        log.warn("关键点类型为null，设置默认值required");
                    }
                    
                    keyPoint.setExampleText(keyPointDTO.getExampleText());
                    keyPoint.setCreatedAt(LocalDateTime.now());
                    keyPoint.setUpdatedAt(LocalDateTime.now());
                    keyPoint.setVersion(1);
                    
                    keyPointRepository.save(keyPoint);
                    log.info("关键点保存成功，ID: {}", keyPoint.getKeyPointId());
                }
            } else {
                log.info("没有关键点需要处理");
            }
            
            return new DataImportResultDTO(true, "标准答案导入成功", 1, 0, 1);
        } catch (Exception e) {
            log.error("导入标准答案失败", e);
            return new DataImportResultDTO(false, "导入失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    @Transactional
    public DataImportResultDTO importStandardAnswers(List<StandardAnswerDTO> answerDTOs) {
        log.info("批量导入标准答案，数量: {}", answerDTOs.size());
        
        int total = answerDTOs.size();
        int imported = 0;
        int failed = 0;
        
        for (StandardAnswerDTO answerDTO : answerDTOs) {
            DataImportResultDTO result = importStandardAnswer(answerDTO);
            if (result.isSuccess()) {
                imported++;
            } else {
                failed++;
            }
        }
        
        return new DataImportResultDTO(failed == 0, "批量导入标准答案完成", imported, failed, total);
    }

    @Override
    @Transactional
    public DataImportResultDTO createStandardQALink(StandardQALinkDTO linkDTO) {
        log.info("创建标准问答对关联，问题ID: {}，答案ID: {}", linkDTO.getStandardQuestionId(), linkDTO.getStandardAnswerId());
        
        try {
            // 检查标准问题是否存在
            Optional<StandardQuestion> questionOpt = standardQuestionRepository.findById(linkDTO.getStandardQuestionId());
            if (!questionOpt.isPresent()) {
                return new DataImportResultDTO(false, "创建关联失败: 标准问题不存在", 0, 1, 1);
            }
            
            // 检查标准答案是否存在
            Optional<StandardAnswer> answerOpt = standardAnswerRepository.findById(linkDTO.getStandardAnswerId());
            if (!answerOpt.isPresent()) {
                return new DataImportResultDTO(false, "创建关联失败: 标准答案不存在", 0, 1, 1);
            }
            
            // 检查原始问题和原始回答
            RawQuestion rawQuestion = null;
            RawAnswer rawAnswer = null;
            
            if (linkDTO.getSourceQuestionId() != null) {
                Optional<RawQuestion> rawQuestionOpt = rawQuestionRepository.findById(linkDTO.getSourceQuestionId());
                if (rawQuestionOpt.isPresent()) {
                    rawQuestion = rawQuestionOpt.get();
                }
            }
            
            if (linkDTO.getSourceAnswerId() != null) {
                Optional<RawAnswer> rawAnswerOpt = rawAnswerRepository.findById(linkDTO.getSourceAnswerId());
                if (rawAnswerOpt.isPresent()) {
                    rawAnswer = rawAnswerOpt.get();
                }
            }
            
            // 检查是否已存在相同的关联
            Optional<StandardQAPair> existingPair = standardQAPairRepository.findByStandardQuestion_StandardQuestionIdAndStandardAnswer_StandardAnswerId(
                    linkDTO.getStandardQuestionId(), 
                    linkDTO.getStandardAnswerId()
            );
            
            if (existingPair.isPresent()) {
                return new DataImportResultDTO(false, "创建关联失败: 该标准问答对关联已存在", 0, 1, 1);
            }
            
            // 创建新的问答对关联
            StandardQAPair qaPair = new StandardQAPair();
            qaPair.setStandardQuestion(questionOpt.get());
            qaPair.setStandardAnswer(answerOpt.get());
            qaPair.setSourceQuestion(rawQuestion);
            qaPair.setSourceAnswer(rawAnswer);
            qaPair.setCreatedAt(LocalDateTime.now());
            qaPair.setUpdatedAt(LocalDateTime.now());
            
            standardQAPairRepository.save(qaPair);
            
            return new DataImportResultDTO(true, "标准问答对关联创建成功", 1, 0, 1);
        } catch (Exception e) {
            log.error("创建标准问答对关联失败", e);
            return new DataImportResultDTO(false, "创建关联失败: " + e.getMessage(), 0, 1, 1);
        }
    }

    @Override
    @Transactional
    public DataImportResultDTO createStandardQALinks(List<StandardQALinkDTO> linkDTOs) {
        log.info("批量创建标准问答对关联，数量: {}", linkDTOs.size());
        
        int total = linkDTOs.size();
        int imported = 0;
        int failed = 0;
        
        for (StandardQALinkDTO linkDTO : linkDTOs) {
            DataImportResultDTO result = createStandardQALink(linkDTO);
            if (result.isSuccess()) {
                imported++;
            } else {
                failed++;
            }
        }
        
        return new DataImportResultDTO(failed == 0, "批量创建标准问答对关联完成", imported, failed, total);
    }
} 