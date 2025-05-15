package com.llmeval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.llmeval.entity.StandardQuestion;
import com.llmeval.mapper.StandardQuestionMapper;
import com.llmeval.service.StandardQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StandardQuestionServiceImpl extends ServiceImpl<StandardQuestionMapper, StandardQuestion> implements StandardQuestionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StandardQuestion createNewVersion(Long questionId, StandardQuestion newQuestion) {
        // 获取当前版本
        StandardQuestion currentVersion = this.getById(questionId);
        if (currentVersion == null) {
            throw new RuntimeException("Question not found");
        }
        
        // 将当前版本标记为非最新
        currentVersion.setIsLatest(false);
        this.updateById(currentVersion);
        
        // 创建新版本
        newQuestion.setVersion(currentVersion.getVersion() + 1);
        newQuestion.setOriginalQuestionId(currentVersion.getOriginalQuestionId());
        newQuestion.setParentQuestionId(currentVersion.getStandardQuestionId());
        newQuestion.setIsLatest(true);
        
        this.save(newQuestion);
        return newQuestion;
    }

    @Override
    public StandardQuestion getLatestVersion(Long originalQuestionId) {
        LambdaQueryWrapper<StandardQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StandardQuestion::getOriginalQuestionId, originalQuestionId)
               .eq(StandardQuestion::getIsLatest, true);
        return this.getOne(wrapper);
    }
} 