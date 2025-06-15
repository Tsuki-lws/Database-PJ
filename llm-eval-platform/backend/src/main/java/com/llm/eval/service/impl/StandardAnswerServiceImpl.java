package com.llm.eval.service.impl;

import com.llm.eval.model.StandardAnswer;
import com.llm.eval.model.AnswerKeyPoint;
import com.llm.eval.repository.StandardAnswerRepository;
import com.llm.eval.repository.StandardQuestionRepository;
import com.llm.eval.repository.AnswerKeyPointRepository;
import com.llm.eval.service.StandardAnswerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StandardAnswerServiceImpl implements StandardAnswerService {
    
    private final StandardAnswerRepository standardAnswerRepository;
    private final StandardQuestionRepository standardQuestionRepository;
    private final AnswerKeyPointRepository answerKeyPointRepository;
    
    @Autowired
    public StandardAnswerServiceImpl(
            StandardAnswerRepository standardAnswerRepository,
            StandardQuestionRepository standardQuestionRepository,
            AnswerKeyPointRepository answerKeyPointRepository) {
        this.standardAnswerRepository = standardAnswerRepository;
        this.standardQuestionRepository = standardQuestionRepository;
        this.answerKeyPointRepository = answerKeyPointRepository;
    }
    
    @Override
    public List<StandardAnswer> getAllStandardAnswers() {
        return standardAnswerRepository.findAll();
    }
    
    @Override
    public List<StandardAnswer> getStandardAnswers(int page, int size, Integer questionId, Boolean isFinal) {
        // 创建分页请求，按创建时间倒序排序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        
        // 使用筛选条件查询
        Page<StandardAnswer> answersPage = standardAnswerRepository.findByFilters(questionId, isFinal, pageable);
        
        return answersPage.getContent();
    }
    
    @Override
    public long countStandardAnswers(Integer questionId, Boolean isFinal) {
        return standardAnswerRepository.countByFilters(questionId, isFinal);
    }
    
    @Override
    public Optional<StandardAnswer> getStandardAnswerById(Integer id) {
        return standardAnswerRepository.findById(id);
    }
    
    @Override
    public List<StandardAnswer> getStandardAnswersByQuestionId(Integer questionId) {
        // Verify that the question exists
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        // 清除一级缓存，确保获取最新数据
        standardAnswerRepository.flush();
        
        List<StandardAnswer> answers = standardAnswerRepository.findByStandardQuestionStandardQuestionId(questionId);
        System.out.println("获取问题的标准答案，问题ID: " + questionId + ", 答案数量: " + answers.size());
        
        // 打印每个答案的isFinal状态
        for (StandardAnswer answer : answers) {
            System.out.println("答案ID: " + answer.getStandardAnswerId() + ", isFinal: " + answer.getIsFinal());
        }
        
        return answers;
    }
    
    @Override
    public Optional<StandardAnswer> getFinalStandardAnswerByQuestionId(Integer questionId) {
        // Verify that the question exists
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        return standardAnswerRepository.findByStandardQuestionStandardQuestionIdAndIsFinalTrue(questionId);
    }
    
    @Override
    @Transactional
    public StandardAnswer createStandardAnswer(StandardAnswer standardAnswer) {
        // Set default values
        if (standardAnswer.getCreatedAt() == null) {
            standardAnswer.setCreatedAt(LocalDateTime.now());
        }
        standardAnswer.setUpdatedAt(LocalDateTime.now());
        standardAnswer.setVersion(1);
        
        // Default to not final
        if (standardAnswer.getIsFinal() == null) {
            standardAnswer.setIsFinal(false);
        }
        
        // Verify that the question exists
        Integer questionId = standardAnswer.getStandardQuestion().getStandardQuestionId();
        if (!standardQuestionRepository.existsById(questionId)) {
            throw new EntityNotFoundException("Standard question not found with id: " + questionId);
        }
        
        return standardAnswerRepository.save(standardAnswer);
    }
    
    @Override
    @Transactional
    public StandardAnswer updateStandardAnswer(Integer id, StandardAnswer standardAnswer) {
        StandardAnswer existingAnswer = standardAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Standard answer not found with id: " + id));
        
        // Update properties
        existingAnswer.setAnswer(standardAnswer.getAnswer());
        existingAnswer.setSourceAnswer(standardAnswer.getSourceAnswer());
        existingAnswer.setSourceType(standardAnswer.getSourceType());
        existingAnswer.setSourceId(standardAnswer.getSourceId());
        existingAnswer.setSelectionReason(standardAnswer.getSelectionReason());
        existingAnswer.setSelectedBy(standardAnswer.getSelectedBy());
        existingAnswer.setUpdatedAt(LocalDateTime.now());
        existingAnswer.setVersion(existingAnswer.getVersion() + 1);
        
        // 更新isFinal状态
        if (standardAnswer.getIsFinal() != null && standardAnswer.getIsFinal()) {
            // 如果设置为最终版本，先取消其他最终版本
            Integer questionId = existingAnswer.getStandardQuestion().getStandardQuestionId();
            Optional<StandardAnswer> existingFinalAnswer = 
                    standardAnswerRepository.findByStandardQuestionStandardQuestionIdAndIsFinalTrue(questionId);
            
            existingFinalAnswer.ifPresent(answer -> {
                if (!answer.getStandardAnswerId().equals(id)) {
                    System.out.println("找到现有最终版本答案，ID: " + answer.getStandardAnswerId() + ", 取消其最终版本标记");
                    answer.setIsFinal(false);
                    answer.setUpdatedAt(LocalDateTime.now());
                    standardAnswerRepository.save(answer);
                }
            });
            
            existingAnswer.setIsFinal(true);
        } else {
            existingAnswer.setIsFinal(standardAnswer.getIsFinal() != null ? standardAnswer.getIsFinal() : existingAnswer.getIsFinal());
        }
        
        // 保存更新后的答案
        StandardAnswer savedAnswer = standardAnswerRepository.save(existingAnswer);
        
        // 处理关键点更新
        if (standardAnswer.getKeyPoints() != null && !standardAnswer.getKeyPoints().isEmpty()) {
            System.out.println("处理关键点更新，关键点数量: " + standardAnswer.getKeyPoints().size());
            
            // 获取现有关键点
            List<AnswerKeyPoint> existingKeyPoints = answerKeyPointRepository.findByStandardAnswerOrderByPointOrder(existingAnswer);
            
            // 遍历提交的关键点
            for (AnswerKeyPoint keyPoint : standardAnswer.getKeyPoints()) {
                if (keyPoint.getKeyPointId() != null) {
                    // 更新现有关键点
                    AnswerKeyPoint existingKeyPoint = existingKeyPoints.stream()
                            .filter(kp -> kp.getKeyPointId().equals(keyPoint.getKeyPointId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (existingKeyPoint != null) {
                        System.out.println("更新现有关键点，ID: " + existingKeyPoint.getKeyPointId());
                        existingKeyPoint.setPointText(keyPoint.getPointText());
                        existingKeyPoint.setPointOrder(keyPoint.getPointOrder());
                        existingKeyPoint.setPointWeight(keyPoint.getPointWeight());
                        existingKeyPoint.setPointType(keyPoint.getPointType());
                        existingKeyPoint.setExampleText(keyPoint.getExampleText());
                        existingKeyPoint.setUpdatedAt(LocalDateTime.now());
                        existingKeyPoint.setVersion(existingKeyPoint.getVersion() + 1);
                        answerKeyPointRepository.save(existingKeyPoint);
                    }
                } else {
                    // 添加新关键点
                    System.out.println("添加新关键点");
                    keyPoint.setStandardAnswer(savedAnswer);
                    keyPoint.setCreatedAt(LocalDateTime.now());
                    keyPoint.setUpdatedAt(LocalDateTime.now());
                    keyPoint.setVersion(1);
                    answerKeyPointRepository.save(keyPoint);
                }
            }
            
            // 删除不再存在的关键点
            for (AnswerKeyPoint existingKeyPoint : existingKeyPoints) {
                boolean stillExists = standardAnswer.getKeyPoints().stream()
                        .anyMatch(kp -> kp.getKeyPointId() != null && kp.getKeyPointId().equals(existingKeyPoint.getKeyPointId()));
                
                if (!stillExists) {
                    System.out.println("删除不再存在的关键点，ID: " + existingKeyPoint.getKeyPointId());
                    answerKeyPointRepository.delete(existingKeyPoint);
                }
            }
        }
        
        return savedAnswer;
    }
    
    @Override
    @Transactional
    public StandardAnswer markAnswerAsFinal(Integer id) {
        StandardAnswer answerToMark = standardAnswerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Standard answer not found with id: " + id));
        
        // Get the question ID
        Integer questionId = answerToMark.getStandardQuestion().getStandardQuestionId();
        System.out.println("标记答案为最终版本，答案ID: " + id + ", 问题ID: " + questionId);
        
        // Find any existing final answers for this question and unmark them
        Optional<StandardAnswer> existingFinalAnswer = 
                standardAnswerRepository.findByStandardQuestionStandardQuestionIdAndIsFinalTrue(questionId);
        
        existingFinalAnswer.ifPresent(answer -> {
            System.out.println("找到现有最终版本答案，ID: " + answer.getStandardAnswerId() + ", 取消其最终版本标记");
            answer.setIsFinal(false);
            answer.setUpdatedAt(LocalDateTime.now());
            StandardAnswer savedAnswer = standardAnswerRepository.save(answer);
            System.out.println("保存取消最终版本标记的答案，ID: " + savedAnswer.getStandardAnswerId() + ", isFinal: " + savedAnswer.getIsFinal());
        });
        
        // Mark the new answer as final
        System.out.println("标记新答案为最终版本，ID: " + answerToMark.getStandardAnswerId());
        answerToMark.setIsFinal(true);
        answerToMark.setUpdatedAt(LocalDateTime.now());
        
        StandardAnswer savedNewFinal = standardAnswerRepository.save(answerToMark);
        System.out.println("保存新的最终版本答案，ID: " + savedNewFinal.getStandardAnswerId() + ", isFinal: " + savedNewFinal.getIsFinal());
        
        // 验证设置是否成功
        List<StandardAnswer> allAnswers = standardAnswerRepository.findByStandardQuestionStandardQuestionId(questionId);
        System.out.println("问题所有答案的最终版本状态:");
        for (StandardAnswer answer : allAnswers) {
            System.out.println("答案ID: " + answer.getStandardAnswerId() + ", isFinal: " + answer.getIsFinal());
        }
        
        return savedNewFinal;
    }
    
    @Override
    @Transactional
    public void deleteStandardAnswer(Integer id) {
        standardAnswerRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public AnswerKeyPoint addKeyPoint(AnswerKeyPoint keyPoint) {
        // 获取标准答案
        StandardAnswer answer = standardAnswerRepository.findById(keyPoint.getStandardAnswer().getStandardAnswerId())
                .orElseThrow(() -> new EntityNotFoundException("Standard answer not found with id: " + 
                        keyPoint.getStandardAnswer().getStandardAnswerId()));
        
        // 设置默认值
        if (keyPoint.getCreatedAt() == null) {
            keyPoint.setCreatedAt(LocalDateTime.now());
        }
        keyPoint.setUpdatedAt(LocalDateTime.now());
        keyPoint.setVersion(1);
        
        // 如果没有设置顺序，则设置为当前最大顺序+1
        if (keyPoint.getPointOrder() == null) {
            List<AnswerKeyPoint> existingPoints = answerKeyPointRepository.findByStandardAnswerOrderByPointOrder(answer);
            int maxOrder = existingPoints.isEmpty() ? 0 : 
                    existingPoints.get(existingPoints.size() - 1).getPointOrder();
            keyPoint.setPointOrder(maxOrder + 1);
        }
        
        // 设置关联的标准答案
        keyPoint.setStandardAnswer(answer);
        
        // 保存关键点
        return answerKeyPointRepository.save(keyPoint);
    }
    
    @Override
    public List<AnswerKeyPoint> getKeyPointsByAnswerId(Integer answerId) {
        // 获取标准答案
        StandardAnswer answer = standardAnswerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Standard answer not found with id: " + answerId));
        
        // 获取关键点列表并按顺序排序
        return answerKeyPointRepository.findByStandardAnswerOrderByPointOrder(answer);
    }
    
    @Override
    @Transactional
    public AnswerKeyPoint updateKeyPoint(Integer id, AnswerKeyPoint keyPoint) {
        // 获取现有关键点
        AnswerKeyPoint existingKeyPoint = answerKeyPointRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Key point not found with id: " + id));
        
        // 更新属性
        existingKeyPoint.setPointText(keyPoint.getPointText());
        existingKeyPoint.setPointOrder(keyPoint.getPointOrder());
        existingKeyPoint.setPointWeight(keyPoint.getPointWeight());
        existingKeyPoint.setPointType(keyPoint.getPointType());
        existingKeyPoint.setExampleText(keyPoint.getExampleText());
        existingKeyPoint.setUpdatedAt(LocalDateTime.now());
        existingKeyPoint.setVersion(existingKeyPoint.getVersion() + 1);
        
        return answerKeyPointRepository.save(existingKeyPoint);
    }
    
    @Override
    @Transactional
    public void deleteKeyPoint(Integer id) {
        answerKeyPointRepository.deleteById(id);
    }
} 