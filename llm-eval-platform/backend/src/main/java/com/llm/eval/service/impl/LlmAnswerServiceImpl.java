package com.llm.eval.service.impl;

import com.llm.eval.dto.LlmAnswerDTO;
import com.llm.eval.model.LlmAnswer;
import com.llm.eval.model.LlmModel;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.EvaluationBatch;
import com.llm.eval.model.DatasetVersion;
import com.llm.eval.repository.LlmAnswerRepository;
import com.llm.eval.repository.LlmModelRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.EvaluationBatchRepository;
import com.llm.eval.repository.DatasetVersionRepository;
import com.llm.eval.service.LlmAnswerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LlmAnswerServiceImpl implements LlmAnswerService {

    private final LlmAnswerRepository llmAnswerRepository;
    private final LlmModelRepository llmModelRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    private final EvaluationBatchRepository batchRepository;
    private final DatasetVersionRepository datasetVersionRepository;

    @Autowired
    public LlmAnswerServiceImpl(
            LlmAnswerRepository llmAnswerRepository,
            LlmModelRepository llmModelRepository,
            StandardQuestionRepository standardQuestionRepository,
            EvaluationBatchRepository batchRepository,
            DatasetVersionRepository datasetVersionRepository) {
        this.llmAnswerRepository = llmAnswerRepository;
        this.llmModelRepository = llmModelRepository;
        this.standardQuestionRepository = standardQuestionRepository;
        this.batchRepository = batchRepository;
        this.datasetVersionRepository = datasetVersionRepository;
    }

    @Override
    public List<LlmAnswerDTO> getAllAnswers() {
        return llmAnswerRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<LlmAnswerDTO> getAnswerById(Integer answerId) {
        return llmAnswerRepository.findById(answerId)
                .map(this::convertToDTO);
    }

    @Override
    public List<LlmAnswerDTO> getAnswersByModelId(Integer modelId) {
        Optional<LlmModel> model = llmModelRepository.findById(modelId);
        if (model.isEmpty()) {
            return new ArrayList<>();
        }
        return llmAnswerRepository.findByModel(model.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LlmAnswerDTO> getAnswersByQuestionId(Integer questionId) {
        Optional<StandardQuestion> question = standardQuestionRepository.findById(questionId);
        if (question.isEmpty()) {
            return new ArrayList<>();
        }
        return llmAnswerRepository.findByStandardQuestion(question.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LlmAnswerDTO> getAnswersByBatchId(Integer batchId) {
        Optional<EvaluationBatch> batch = batchRepository.findById(batchId);
        if (batch.isEmpty()) {
            return new ArrayList<>();
        }
        return llmAnswerRepository.findByBatch(batch.get())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LlmAnswerDTO createAnswer(LlmAnswerDTO answerDTO) {
        LlmAnswer answer = convertToEntity(answerDTO);
        answer = llmAnswerRepository.save(answer);
        return convertToDTO(answer);
    }

    @Override
    @Transactional
    public Optional<LlmAnswerDTO> updateAnswer(Integer answerId, LlmAnswerDTO answerDTO) {
        if (!llmAnswerRepository.existsById(answerId)) {
            return Optional.empty();
        }
        
        LlmAnswer answer = convertToEntity(answerDTO);
        answer.setLlmAnswerId(answerId);
        answer = llmAnswerRepository.save(answer);
        
        return Optional.of(convertToDTO(answer));
    }

    @Override
    @Transactional
    public boolean deleteAnswer(Integer answerId) {
        if (!llmAnswerRepository.existsById(answerId)) {
            return false;
        }
        
        llmAnswerRepository.deleteById(answerId);
        return true;
    }

    @Override
    @Transactional
    public List<LlmAnswerDTO> generateAnswersForBatch(Integer batchId) {
        // 这里应该实现批量生成回答的逻辑
        // 由于这是一个复杂的功能，这里只返回空列表作为占位符
        return new ArrayList<>();
    }
    
    @Override
    public List<Map<String, Object>> getDatasetVersionsWithAnswers() {
        // 获取所有数据集版本
        List<DatasetVersion> datasets = datasetVersionRepository.findAll();
        
        // 转换为前端需要的格式
        return datasets.stream().map(dataset -> {
            Map<String, Object> datasetInfo = new HashMap<>();
            datasetInfo.put("datasetId", dataset.getVersionId());
            datasetInfo.put("name", dataset.getName());
            // 数据集版本没有version字段，使用versionId代替
            datasetInfo.put("version", dataset.getVersionId());
            datasetInfo.put("isPublished", dataset.getIsPublished());
            datasetInfo.put("createdAt", dataset.getCreatedAt());
            
            // 获取该数据集中的问题数量
            long questionCount = dataset.getQuestions() != null ? dataset.getQuestions().size() : 0;
            datasetInfo.put("questionCount", questionCount);
            
            // 获取该数据集中的模型回答数量 - 使用Repository方法
            long answerCount = llmAnswerRepository.countByDatasetVersion(dataset);
            datasetInfo.put("answerCount", answerCount);
            
            return datasetInfo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getQuestionsWithAnswersInDataset(Integer datasetId, int page, int size) {
        // 使用新的方法实现，不过滤关键词和回答状态
        return getQuestionsWithAnswersInDataset(datasetId, page, size, null, null);
    }
    
    @Override
    public Map<String, Object> getQuestionsWithAnswersInDataset(Integer datasetId, int page, int size, String keyword, Boolean hasAnswer) {
        // 获取数据集
        Optional<DatasetVersion> datasetOpt = datasetVersionRepository.findById(datasetId);
        if (datasetOpt.isEmpty()) {
            return Map.of("error", "数据集不存在");
        }
        
        DatasetVersion dataset = datasetOpt.get();
        
        // 获取该数据集中的所有问题
        List<StandardQuestion> questions = new ArrayList<>(dataset.getQuestions());
        
        // 按关键词过滤
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase().trim();
            questions = questions.stream()
                    .filter(q -> q.getQuestion() != null && q.getQuestion().toLowerCase().contains(lowerKeyword))
                    .collect(Collectors.toList());
        }
        
        // 计算总问题数
        int totalBeforeAnswerFilter = questions.size();
        
        // 按回答状态过滤
        if (hasAnswer != null) {
            List<StandardQuestion> filteredQuestions = new ArrayList<>();
            
            for (StandardQuestion question : questions) {
                // 获取该问题在当前数据集中的回答
                List<LlmAnswer> answers = llmAnswerRepository.findByStandardQuestionAndDatasetVersion(question, dataset);
                
                // 根据hasAnswer参数过滤
                if ((hasAnswer && !answers.isEmpty()) || (!hasAnswer && answers.isEmpty())) {
                    filteredQuestions.add(question);
                }
            }
            
            questions = filteredQuestions;
        }
        
        // 计算分页信息
        int total = questions.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);
        
        // 手动分页
        List<StandardQuestion> pagedQuestions = fromIndex < total ? 
                questions.subList(fromIndex, toIndex) : new ArrayList<>();
        
        // 转换为前端需要的格式
        List<Map<String, Object>> questionList = pagedQuestions.stream().map(question -> {
            Map<String, Object> questionInfo = new HashMap<>();
            questionInfo.put("questionId", question.getStandardQuestionId());
            questionInfo.put("question", question.getQuestion());
            questionInfo.put("questionType", question.getQuestionType());
            questionInfo.put("difficulty", question.getDifficulty());
            
            // 获取该问题的所有模型回答 - 使用Repository方法
            List<LlmAnswer> answers = llmAnswerRepository.findByStandardQuestionAndDatasetVersion(question, dataset);
            
            // 统计模型回答信息
            long answerCount = answers.size();
            List<String> modelNames = answers.stream()
                    .filter(answer -> answer.getModel() != null)
                    .map(answer -> answer.getModel().getName() + " " + 
                         (answer.getModel().getVersion() != null ? answer.getModel().getVersion() : ""))
                    .distinct()
                    .collect(Collectors.toList());
            
            questionInfo.put("answerCount", answerCount);
            questionInfo.put("models", modelNames);
            
            return questionInfo;
        }).collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("content", questionList);
        result.put("totalElements", total);
        result.put("totalPages", totalPages);
        result.put("size", size);
        result.put("number", page);
        result.put("dataset", Map.of(
            "datasetId", dataset.getVersionId(),
            "name", dataset.getName(),
            // 数据集版本没有version字段，使用versionId代替
            "version", dataset.getVersionId(),
            "isPublished", dataset.getIsPublished()
        ));
        
        return result;
    }
    
    @Override
    public List<LlmAnswerDTO> getModelAnswersForQuestionInDataset(Integer datasetId, Integer questionId) {
        // 获取数据集
        Optional<DatasetVersion> datasetOpt = datasetVersionRepository.findById(datasetId);
        if (datasetOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取问题
        Optional<StandardQuestion> questionOpt = standardQuestionRepository.findById(questionId);
        if (questionOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取该数据集中该问题的所有模型回答 - 使用Repository方法
        List<LlmAnswer> answers = llmAnswerRepository.findByStandardQuestionAndDatasetVersion(
                questionOpt.get(), datasetOpt.get());
        
        // 转换为DTO
        return answers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private LlmAnswerDTO convertToDTO(LlmAnswer entity) {
        if (entity == null) {
            return null;
        }
        
        LlmAnswerDTO dto = new LlmAnswerDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 手动处理复杂属性
        if (entity.getModel() != null) {
            dto.setModel(convertModelToDTO(entity.getModel()));
        }
        
        if (entity.getStandardQuestion() != null) {
            dto.setStandardQuestion(convertQuestionToDTO(entity.getStandardQuestion()));
        }
        
        if (entity.getBatch() != null) {
            dto.setBatchId(entity.getBatch().getBatchId());
        }
        
        if (entity.getParentAnswer() != null) {
            dto.setParentAnswerId(entity.getParentAnswer().getLlmAnswerId());
        }
        
        return dto;
    }
    
    private LlmAnswer convertToEntity(LlmAnswerDTO dto) {
        if (dto == null) {
            return null;
        }
        
        LlmAnswer entity = new LlmAnswer();
        
        // 复制基本属性
        entity.setLlmAnswerId(dto.getLlmAnswerId());
        entity.setContent(dto.getContent());
        entity.setLatency(dto.getLatency());
        entity.setTokensUsed(dto.getTokensUsed());
        entity.setPromptTemplate(dto.getPromptTemplate());
        entity.setPromptParams(dto.getPromptParams());
        entity.setTemperature(dto.getTemperature());
        entity.setTopP(dto.getTopP());
        entity.setRetryCount(dto.getRetryCount());
        entity.setIsFinal(dto.getIsFinal());
        entity.setCreatedAt(dto.getCreatedAt());
        
        // 处理关联实体
        if (dto.getModel() != null && dto.getModel().getModelId() != null) {
            llmModelRepository.findById(dto.getModel().getModelId())
                    .ifPresent(entity::setModel);
        }
        
        if (dto.getStandardQuestion() != null && dto.getStandardQuestion().getStandardQuestionId() != null) {
            standardQuestionRepository.findById(dto.getStandardQuestion().getStandardQuestionId())
                    .ifPresent(entity::setStandardQuestion);
        }
        
        // 设置一个默认的数据集版本（如果需要）
        // 尝试获取最新的数据集版本，如果没有则创建一个临时的
        datasetVersionRepository.findAll().stream()
                .findFirst()
                .ifPresentOrElse(
                        entity::setDatasetVersion,
                        () -> {
                            DatasetVersion defaultVersion = new DatasetVersion();
                            defaultVersion.setVersionId(1);
                            defaultVersion.setName("默认版本");
                            defaultVersion.setIsPublished(true);
                            entity.setDatasetVersion(defaultVersion);
                        });
        
        if (dto.getBatchId() != null) {
            batchRepository.findById(dto.getBatchId())
                    .ifPresent(entity::setBatch);
        }
        
        if (dto.getParentAnswerId() != null) {
            llmAnswerRepository.findById(dto.getParentAnswerId())
                    .ifPresent(entity::setParentAnswer);
        }
        
        return entity;
    }
    
    // 辅助方法，转换LlmModel到DTO
    private com.llm.eval.dto.LlmModelDTO convertModelToDTO(LlmModel model) {
        com.llm.eval.dto.LlmModelDTO modelDTO = new com.llm.eval.dto.LlmModelDTO();
        modelDTO.setModelId(model.getModelId());
        modelDTO.setName(model.getName());
        modelDTO.setVersion(model.getVersion());
        modelDTO.setProvider(model.getProvider());
        modelDTO.setDescription(model.getDescription());
        modelDTO.setApiConfig(model.getApiConfig());
        modelDTO.setCreatedAt(model.getCreatedAt());
        return modelDTO;
    }
    
    // 辅助方法，转换StandardQuestion到DTO
    private com.llm.eval.dto.StandardQuestionDTO convertQuestionToDTO(StandardQuestion question) {
        com.llm.eval.dto.StandardQuestionDTO questionDTO = new com.llm.eval.dto.StandardQuestionDTO();
        questionDTO.setStandardQuestionId(question.getStandardQuestionId());
        questionDTO.setQuestion(question.getQuestion());
        questionDTO.setQuestionType(question.getQuestionType() != null ? question.getQuestionType().toString() : null);
        questionDTO.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().toString() : null);
        return questionDTO;
    }
} 