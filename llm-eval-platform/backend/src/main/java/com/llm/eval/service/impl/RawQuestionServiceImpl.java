package com.llm.eval.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import com.llm.eval.dto.QueryParams;
import com.llm.eval.dto.RawQuestionConverter;
import com.llm.eval.model.RawQuestion;
import com.llm.eval.model.StandardQuestion;
import com.llm.eval.model.QuestionCategory;
import com.llm.eval.repository.RawQuestionRepository;
import com.llm.eval.service.RawQuestionService;
import com.llm.eval.service.StandardQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class RawQuestionServiceImpl implements RawQuestionService {

    private static final Logger logger = LoggerFactory.getLogger(RawQuestionServiceImpl.class);
    
    private final RawQuestionRepository rawQuestionRepository;
    private final StandardQuestionService standardQuestionService;
    
    // 简单的内存缓存
    private final Map<Integer, RawQuestion> questionCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> sourcesCache = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Long>> statsCache = new ConcurrentHashMap<>();
    
    // 缓存过期时间 (毫秒)
    private static final long CACHE_EXPIRY = TimeUnit.MINUTES.toMillis(5);
    private long sourcesCacheTime = 0;
    private long statsCacheTime = 0;

    @Autowired
    public RawQuestionServiceImpl(RawQuestionRepository rawQuestionRepository, 
                                StandardQuestionService standardQuestionService) {
        this.rawQuestionRepository = rawQuestionRepository;
        this.standardQuestionService = standardQuestionService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RawQuestion> findRawQuestionsByPage(QueryParams queryParams, Pageable pageable) {
        try {
            logger.debug("开始查询原始问题列表，参数: {}, 分页: {}", queryParams, pageable);
            
            // 使用简单条件构建查询，避免复杂的Specification
            String keyword = queryParams.getKeyword();
            String source = queryParams.getSource();
            
            List<RawQuestion> questions;
            long total = 0;
            
            // 根据条件选择不同的查询方法，避免创建动态Specification
            if (keyword != null && !keyword.isEmpty() && source != null && !source.isEmpty()) {
                // 关键词和来源都不为空
                logger.debug("使用关键词和来源查询：keyword={}, source={}", keyword, source);
                questions = rawQuestionRepository.findByKeywordAndSource(
                    "%" + keyword + "%", 
                    "%" + keyword + "%", 
                    source, 
                    pageable
                );
                total = rawQuestionRepository.countByKeywordAndSource(
                    "%" + keyword + "%", 
                    "%" + keyword + "%", 
                    source
                );
            } else if (keyword != null && !keyword.isEmpty()) {
                // 只有关键词不为空
                logger.debug("使用关键词查询：keyword={}", keyword);
                questions = rawQuestionRepository.findByKeyword(
                    "%" + keyword + "%", 
                    "%" + keyword + "%", 
                    pageable
                );
                total = rawQuestionRepository.countByKeyword(
                    "%" + keyword + "%", 
                    "%" + keyword + "%"
                );
            } else if (source != null && !source.isEmpty()) {
                // 只有来源不为空
                logger.debug("使用来源查询：source={}", source);
                questions = rawQuestionRepository.findBySource(source, pageable);
                total = rawQuestionRepository.countBySource(source);
            } else {
                // 都为空，查询所有
                logger.debug("查询所有问题");
                questions = rawQuestionRepository.findAllQuestions(pageable);
                total = rawQuestionRepository.countAllQuestions();
            }
            
            // 创建Page对象手动封装结果
            Page<RawQuestion> result = new PageImpl<>(
                questions,
                pageable,
                total
            );
            
            logger.debug("查询完成，返回结果数量: {}, 总数: {}", questions.size(), total);
            return result;
        } catch (Exception e) {
            logger.error("查询原始问题列表失败", e);
            // 返回空页面，而不是抛出异常
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RawQuestion> findRawQuestionById(Integer id) {
        try {
            logger.debug("查询原始问题详情，ID: {}", id);
            return rawQuestionRepository.findById(id);
        } catch (Exception e) {
            logger.error("查询原始问题详情失败，ID: {}", id, e);
            throw new RuntimeException("查询原始问题详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    public RawQuestion saveRawQuestion(RawQuestion rawQuestion) {
        try {
            logger.debug("保存原始问题: {}", rawQuestion);
            return rawQuestionRepository.save(rawQuestion);
        } catch (Exception e) {
            logger.error("保存原始问题失败", e);
            throw new RuntimeException("保存原始问题失败: " + e.getMessage(), e);
        }
    }

    @Override
    public RawQuestion updateRawQuestion(RawQuestion rawQuestion) {
        // 确保问题存在
        if (!rawQuestionRepository.existsById(rawQuestion.getQuestionId())) {
            throw new RuntimeException("原始问题不存在: " + rawQuestion.getQuestionId());
        }
        try {
            logger.debug("更新原始问题: {}", rawQuestion);
            return rawQuestionRepository.save(rawQuestion);
        } catch (Exception e) {
            logger.error("更新原始问题失败", e);
            throw new RuntimeException("更新原始问题失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRawQuestion(Integer id) {
        try {
            logger.debug("删除原始问题，ID: {}", id);
            rawQuestionRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("删除原始问题失败，ID: {}", id, e);
            throw new RuntimeException("删除原始问题失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllSources() {
        try {
            logger.debug("获取所有问题来源");
            return rawQuestionRepository.findDistinctSources();
        } catch (Exception e) {
            logger.error("获取所有问题来源失败", e);
            throw new RuntimeException("获取所有问题来源失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getQuestionCountsBySource() {
        try {
            logger.debug("获取各来源问题数量");
            Map<String, Long> result = rawQuestionRepository.countBySource();
            logger.debug("获取各来源问题数量完成: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("获取各来源问题数量失败", e);
            throw new RuntimeException("获取各来源问题数量失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> convertToStandardQuestion(Integer id, RawQuestionConverter converter) {
        // 查询原始问题
        RawQuestion rawQuestion = findRawQuestionById(id)
                .orElseThrow(() -> new RuntimeException("原始问题不存在: " + id));
        
        try {
            logger.debug("将原始问题转换为标准问题，原始问题ID: {}, 转换参数: {}", id, converter);
            
            // 准备标准问题
            StandardQuestion standardQuestion = new StandardQuestion();
            
            // 设置问题文本（使用自定义文本或原始文本）
            if (StringUtils.hasText(converter.getCustomQuestion())) {
                standardQuestion.setQuestion(converter.getCustomQuestion());
            } else {
                standardQuestion.setQuestion(rawQuestion.getQuestionBody());
            }
            
            // 设置分类、难度和类型
            // 查询分类对象
            if (converter.getCategoryId() != null) {
                QuestionCategory category = new QuestionCategory();
                category.setCategoryId(converter.getCategoryId());
                standardQuestion.setCategory(category);
            }
            
            // 设置难度（将字符串转换为枚举）
            if (converter.getDifficulty() != null) {
                try {
                    StandardQuestion.DifficultyLevel difficultyLevel = 
                        StandardQuestion.DifficultyLevel.valueOf(converter.getDifficulty());
                    standardQuestion.setDifficulty(difficultyLevel);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("无效的难度值: " + converter.getDifficulty());
                }
            }
            
            // 设置问题类型（将字符串转换为枚举）
            if (converter.getQuestionType() != null) {
                try {
                    StandardQuestion.QuestionType questionType = 
                        StandardQuestion.QuestionType.valueOf(converter.getQuestionType());
                    standardQuestion.setQuestionType(questionType);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("无效的问题类型: " + converter.getQuestionType());
                }
            }
            
            // 设置来源问题关联
            standardQuestion.setSourceQuestion(rawQuestion);
            
            // 设置默认状态
            standardQuestion.setStatus(StandardQuestion.QuestionStatus.draft);
            
            // 保存标准问题
            StandardQuestion savedQuestion = standardQuestionService.createStandardQuestion(standardQuestion);
            
            // 关联标签
            if (converter.getTagIds() != null && !converter.getTagIds().isEmpty()) {
                // 使用接口中的方法添加标签
                standardQuestionService.addTagsToQuestion(savedQuestion.getStandardQuestionId(), 
                    new HashSet<>(converter.getTagIds()));
            }
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("standardQuestion", savedQuestion);
            result.put("success", true);
            
            logger.debug("转换标准问题成功，标准问题ID: {}", savedQuestion.getStandardQuestionId());
            return result;
        } catch (Exception e) {
            logger.error("转换标准问题失败", e);
            throw new RuntimeException("转换标准问题失败: " + e.getMessage(), e);
        }
    }
} 