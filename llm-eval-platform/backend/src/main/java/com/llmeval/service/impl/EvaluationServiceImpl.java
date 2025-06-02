package com.llmeval.service.impl;

import com.llmeval.dto.EvaluationDetailDTO;
import com.llmeval.dto.ManualEvaluationDTO;
import com.llmeval.dto.UnevaluatedAnswerDTO;
import com.llmeval.entity.Evaluation;
import com.llmeval.entity.LlmAnswer;
import com.llmeval.entity.StandardAnswer;
import com.llmeval.entity.StandardQuestion;
import com.llmeval.repository.EvaluationRepository;
import com.llmeval.repository.LlmAnswerRepository;
import com.llmeval.repository.StandardAnswerRepository;
import com.llmeval.repository.StandardQuestionRepository;
import com.llmeval.service.EvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 评测服务实现类
 */
@Service
@Slf4j
public class EvaluationServiceImpl implements EvaluationService {

    @Autowired
    private LlmAnswerRepository llmAnswerRepository;

    @Autowired
    private StandardQuestionRepository standardQuestionRepository;

    @Autowired
    private StandardAnswerRepository standardAnswerRepository;

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Override
    public Page<UnevaluatedAnswerDTO> getUnevaluatedAnswers(Integer modelId, String questionType, Integer categoryId, Pageable pageable) {
        // 这里应该是从数据库中查询未评测的答案
        // 为了演示，我们返回模拟数据
        List<UnevaluatedAnswerDTO> mockData = new ArrayList<>();
        
        UnevaluatedAnswerDTO dto1 = new UnevaluatedAnswerDTO();
        dto1.setAnswerId(1);
        dto1.setQuestionId(101);
        dto1.setQuestion("什么是大语言模型？");
        dto1.setModelName("GPT-4");
        dto1.setModelVersion("0613");
        dto1.setQuestionType("subjective");
        dto1.setStatus("pending");
        dto1.setCategoryName("人工智能");
        mockData.add(dto1);
        
        UnevaluatedAnswerDTO dto2 = new UnevaluatedAnswerDTO();
        dto2.setAnswerId(2);
        dto2.setQuestionId(102);
        dto2.setQuestion("以下哪个不是常见的深度学习框架？");
        dto2.setModelName("Claude 2");
        dto2.setModelVersion("");
        dto2.setQuestionType("single_choice");
        dto2.setStatus("pending");
        dto2.setCategoryName("编程");
        mockData.add(dto2);
        
        // 实际实现应该是从数据库中分页查询，这里简化处理
        return new org.springframework.data.domain.PageImpl<>(mockData, pageable, mockData.size());
    }

    @Override
    public EvaluationDetailDTO getEvaluationDetail(Integer answerId) {
        // 这里应该是从数据库中查询评测详情
        // 为了演示，我们返回模拟数据
        EvaluationDetailDTO dto = new EvaluationDetailDTO();
        dto.setAnswerId(answerId);
        
        if (answerId == 1) {
            dto.setQuestion("什么是大语言模型？");
            dto.setQuestionType("subjective");
            dto.setModelName("GPT-4");
            dto.setModelVersion("0613");
            dto.setModelAnswer("大语言模型（Large Language Model，LLM）是一种基于深度学习的自然语言处理模型，它通过在海量文本数据上训练，能够理解和生成人类语言。这类模型通常采用Transformer架构，具有数十亿甚至数千亿参数，能够执行各种语言任务，如文本生成、翻译、问答和摘要等。");
            dto.setStandardAnswer("大语言模型（Large Language Model，LLM）是一种基于深度学习的自然语言处理模型，通过在海量文本数据上预训练，习得语言的统计规律和知识。其特点包括：\n1. 采用Transformer架构，具有数十亿到数千亿参数\n2. 具备强大的语言理解和生成能力\n3. 能够执行多种任务，如文本生成、翻译、问答、摘要等\n4. 通过自监督学习方式训练，预测上下文中的词语\n5. 可通过微调适应特定领域或任务");
            dto.setCategoryName("人工智能");
            
            List<EvaluationDetailDTO.KeyPointDTO> keyPoints = new ArrayList<>();
            EvaluationDetailDTO.KeyPointDTO kp1 = new EvaluationDetailDTO.KeyPointDTO();
            kp1.setKeyPointId(1);
            kp1.setPointText("定义准确（基于深度学习的NLP模型）");
            kp1.setPointWeight(1.0);
            kp1.setPointType("required");
            keyPoints.add(kp1);
            
            EvaluationDetailDTO.KeyPointDTO kp2 = new EvaluationDetailDTO.KeyPointDTO();
            kp2.setKeyPointId(2);
            kp2.setPointText("提到海量文本数据训练");
            kp2.setPointWeight(1.0);
            kp2.setPointType("required");
            keyPoints.add(kp2);
            
            EvaluationDetailDTO.KeyPointDTO kp3 = new EvaluationDetailDTO.KeyPointDTO();
            kp3.setKeyPointId(3);
            kp3.setPointText("提到Transformer架构");
            kp3.setPointWeight(0.8);
            kp3.setPointType("bonus");
            keyPoints.add(kp3);
            
            EvaluationDetailDTO.KeyPointDTO kp4 = new EvaluationDetailDTO.KeyPointDTO();
            kp4.setKeyPointId(4);
            kp4.setPointText("提到模型规模（参数量）");
            kp4.setPointWeight(0.8);
            kp4.setPointType("bonus");
            keyPoints.add(kp4);
            
            EvaluationDetailDTO.KeyPointDTO kp5 = new EvaluationDetailDTO.KeyPointDTO();
            kp5.setKeyPointId(5);
            kp5.setPointText("列举应用场景");
            kp5.setPointWeight(1.0);
            kp5.setPointType("required");
            keyPoints.add(kp5);
            
            dto.setKeyPoints(keyPoints);
        } else if (answerId == 2) {
            dto.setQuestion("以下哪个不是常见的深度学习框架？");
            dto.setQuestionType("single_choice");
            dto.setModelName("Claude 2");
            dto.setModelVersion("");
            dto.setModelAnswer("D. DeepMind");
            dto.setStandardAnswer("D. DeepMind");
            dto.setCategoryName("编程");
            
            List<EvaluationDetailDTO.OptionDTO> options = new ArrayList<>();
            EvaluationDetailDTO.OptionDTO op1 = new EvaluationDetailDTO.OptionDTO();
            op1.setOptionId(1);
            op1.setOptionCode("A");
            op1.setOptionText("TensorFlow");
            op1.setIsCorrect(false);
            options.add(op1);
            
            EvaluationDetailDTO.OptionDTO op2 = new EvaluationDetailDTO.OptionDTO();
            op2.setOptionId(2);
            op2.setOptionCode("B");
            op2.setOptionText("PyTorch");
            op2.setIsCorrect(false);
            options.add(op2);
            
            EvaluationDetailDTO.OptionDTO op3 = new EvaluationDetailDTO.OptionDTO();
            op3.setOptionId(3);
            op3.setOptionCode("C");
            op3.setOptionText("Keras");
            op3.setIsCorrect(false);
            options.add(op3);
            
            EvaluationDetailDTO.OptionDTO op4 = new EvaluationDetailDTO.OptionDTO();
            op4.setOptionId(4);
            op4.setOptionCode("D");
            op4.setOptionText("DeepMind");
            op4.setIsCorrect(true);
            options.add(op4);
            
            dto.setOptions(options);
        }
        
        return dto;
    }

    @Override
    @Transactional
    public void submitManualEvaluation(ManualEvaluationDTO evaluationDTO) {
        // 实际实现应该是保存评测结果到数据库
        log.info("提交人工评测结果: {}", evaluationDTO);
        
        // 1. 获取LLM回答
        LlmAnswer llmAnswer = llmAnswerRepository.findById(evaluationDTO.getAnswerId())
                .orElseThrow(() -> new RuntimeException("LLM回答不存在"));
        
        // 2. 获取标准答案
        StandardQuestion question = standardQuestionRepository.findById(llmAnswer.getStandardQuestionId())
                .orElseThrow(() -> new RuntimeException("标准问题不存在"));
        
        StandardAnswer standardAnswer = standardAnswerRepository.findByStandardQuestionIdAndIsFinal(
                question.getStandardQuestionId(), true)
                .orElseThrow(() -> new RuntimeException("标准答案不存在"));
        
        // 3. 创建评测记录
        Evaluation evaluation = new Evaluation();
        evaluation.setLlmAnswerId(llmAnswer.getLlmAnswerId());
        evaluation.setStandardAnswerId(standardAnswer.getStandardAnswerId());
        evaluation.setScore(evaluationDTO.getScore());
        evaluation.setMethod("human");
        evaluation.setComments(evaluationDTO.getComments());
        evaluation.setCreatedAt(LocalDateTime.now());
        evaluation.setUpdatedAt(LocalDateTime.now());
        
        // 4. 保存评测记录
        evaluationRepository.save(evaluation);
        
        // 5. 如果有关键点评估，保存关键点评估结果
        if (evaluationDTO.getKeyPointsStatus() != null && !evaluationDTO.getKeyPointsStatus().isEmpty()) {
            // 实际实现应该是保存关键点评估结果
            log.info("保存关键点评估结果: {}", evaluationDTO.getKeyPointsStatus());
        }
    }

    @Override
    public Map<String, Object> importEvaluations(MultipartFile file) {
        // 实际实现应该是解析文件并导入评测结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "评测结果导入成功");
        result.put("total", 100);
        result.put("imported", 98);
        result.put("failed", 2);
        return result;
    }

    @Override
    public Map<String, Object> getModelComparisonData(List<Integer> modelIds) {
        // 实际实现应该是从数据库中查询模型对比数据
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> models = new ArrayList<>();
        Map<String, Object> model1 = new HashMap<>();
        model1.put("modelId", 1);
        model1.put("name", "GPT-4");
        model1.put("version", "0613");
        model1.put("overallScore", 8.75);
        model1.put("categoryScores", Map.of(
            "编程", 9.2,
            "数学", 8.5,
            "推理", 8.8,
            "知识", 9.0,
            "创意", 8.3
        ));
        model1.put("difficultyScores", Map.of(
            "easy", 9.5,
            "medium", 8.8,
            "hard", 7.9
        ));
        models.add(model1);
        
        Map<String, Object> model2 = new HashMap<>();
        model2.put("modelId", 2);
        model2.put("name", "Claude 2");
        model2.put("version", "");
        model2.put("overallScore", 8.42);
        model2.put("categoryScores", Map.of(
            "编程", 8.8,
            "数学", 8.2,
            "推理", 8.6,
            "知识", 8.7,
            "创意", 7.9
        ));
        model2.put("difficultyScores", Map.of(
            "easy", 9.2,
            "medium", 8.5,
            "hard", 7.6
        ));
        models.add(model2);
        
        result.put("models", models);
        return result;
    }
} 