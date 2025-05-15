package com.llmeval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.llmeval.entity.StandardQuestion;

public interface StandardQuestionService extends IService<StandardQuestion> {
    // 创建新版本的标准问题
    StandardQuestion createNewVersion(Long questionId, StandardQuestion newQuestion);
    
    // 获取最新版本的标准问题
    StandardQuestion getLatestVersion(Long originalQuestionId);
} 