```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'primaryColor': '#ffffff',
    'primaryTextColor': '#2d3748',
    'primaryBorderColor': '#4a5568',
    'lineColor': '#718096',
    'secondaryColor': '#f7fafc',
    'tertiaryColor': '#edf2f7',
    'background': '#ffffff',
    'mainBkg': '#f7fafc',
    'secondBkg': '#edf2f7'
  },
  'er': {
    'fontSize': 16,
    'entityColor': '#e2e8f0',
    'entityBkg': '#f7fafc',
    'entityTextColor': '#2d3748',
    'relationColor': '#4a5568',
    'attributeColor': '#2d3748'
  }
}}%%
erDiagram    %% ===========================================
    %% 核心用户和权限管理
    %% ===========================================
    users {
        int user_id PK "用户ID"
        varchar username "用户名"
        varchar password "密码"
        enum user_type "用户类型"
        varchar email "邮箱"
        text profile "个人简介"
        text expertise_areas "专业领域"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
    }
    
    %% ===========================================
    %% 原始数据层
    %% ===========================================
    raw_questions {
        int question_id PK "问题ID"
        int source_question_id "源问题ID"
        text question_title "问题标题"
        text question_body "问题内容"
        varchar source "数据源"
        int source_id "源系统ID"
        varchar source_url "源链接"
        timestamp crawled_at "爬取时间"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
    }
    
    raw_answers {
        int answer_id PK "答案ID"
        int source_answer_id "源答案ID"
        int question_id FK "关联问题"
        text answer_body "答案内容"
        varchar author_info "作者信息"
        int upvotes "点赞数"
        boolean is_accepted "是否被采纳"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
    }
    
    %% ===========================================
    %% 分类和标签系统
    %% ===========================================
    question_categories {
        int category_id PK "分类ID"
        varchar name "分类名称"
        text description "分类描述"
        int parent_id FK "父分类ID"
    }
    
    tags {
        int tag_id PK "标签ID"
        varchar tag_name "标签名称"
        text description "标签描述"
        varchar color "标签颜色"
    }    
    %% ===========================================
    %% 标准问题管理
    %% ===========================================
    standard_questions {
        int standard_question_id PK "标准问题ID"
        text question "问题内容"
        int category_id FK "分类ID"
        enum question_type "问题类型"
        enum difficulty "难度级别"
        int source_question_id FK "源问题ID"
        enum status "状态"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
        int version "版本号"
    }
    
    standard_question_versions {
        int version_id PK "版本ID"
        int standard_question_id FK "标准问题ID"
        text question "问题内容"
        int category_id "分类ID"
        enum question_type "问题类型"
        enum difficulty "难度级别"
        int version "版本号"
        int changed_by FK "修改者ID"
        text change_reason "修改原因"
        timestamp created_at "创建时间"
    }
    
    standard_question_tags {
        int mapping_id PK "映射ID"
        int standard_question_id FK "标准问题ID"
        int tag_id FK "标签ID"
    }
    
    %% ===========================================
    %% 标准答案管理
    %% ===========================================
    standard_answers {
        int standard_answer_id PK "标准答案ID"
        int standard_question_id FK "标准问题ID"
        text answer "答案内容"
        int source_answer_id FK "源答案ID"
        enum source_type "来源类型"
        int source_id "来源ID"
        text selection_reason "选择原因"
        int selected_by FK "选择者ID"
        boolean is_final "是否最终版本"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
        int version "版本号"
    }
    
    standard_answer_versions {
        int version_id PK "版本ID"
        int standard_answer_id FK "标准答案ID"
        text answer "答案内容"
        int version "版本号"
        int changed_by FK "修改者ID"
        text change_reason "修改原因"
        timestamp created_at "创建时间"
    }    
    %% ===========================================
    %% 答案关键点管理
    %% ===========================================
    answer_key_points {
        int key_point_id PK "关键点ID"
        int standard_answer_id FK "标准答案ID"
        text point_text "关键点文本"
        int point_order "排序"
        decimal point_weight "权重"
        enum point_type "关键点类型"
        text example_text "示例文本"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
        int version "版本号"
    }
    
    key_point_versions {
        int version_id PK "版本ID"
        int key_point_id FK "关键点ID"
        text point_text "关键点文本"
        decimal point_weight "权重"
        enum point_type "关键点类型"
        text example_text "示例文本"
        int version "版本号"
        int changed_by FK "修改者ID"
        text change_reason "修改原因"
        timestamp created_at "创建时间"
    }
    
    %% ===========================================
    %% 选择题选项管理
    %% ===========================================
    objective_question_options {
        int option_id PK "选项ID"
        int standard_question_id FK "标准问题ID"
        text option_text "选项文本"
        varchar option_code "选项代码"
        boolean is_correct "是否正确"
        text explanation "解释说明"
        int option_order "选项顺序"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
        int version "版本号"
    }
    
    option_versions {
        int version_id PK "版本ID"
        int option_id FK "选项ID"
        text option_text "选项文本"
        varchar option_code "选项代码"
        boolean is_correct "是否正确"
        text explanation "解释说明"
        int version "版本号"
        int changed_by FK "修改者ID"
        text change_reason "修改原因"
        timestamp created_at "创建时间"
    }    
    %% ===========================================
    %% 问答对和数据集管理
    %% ===========================================
    standard_qa_pairs {
        int qa_id PK "问答对ID"
        int standard_question_id FK "标准问题ID"
        int standard_answer_id FK "标准答案ID"
        int source_question_id FK "源问题ID"
        int source_answer_id FK "源答案ID"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }
    
    dataset_versions {
        int version_id PK "数据集版本ID"
        varchar name "数据集名称"
        text description "数据集描述"
        date release_date "发布日期"
        boolean is_published "是否已发布"
        int question_count "问题数量"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }
    
    dataset_question_mapping {
        int mapping_id PK "映射ID"
        int version_id FK "数据集版本ID"
        int standard_question_id FK "标准问题ID"
    }
    
    %% ===========================================
    %% LLM模型和评测管理
    %% ===========================================
    llm_models {
        int model_id PK "模型ID"
        varchar name "模型名称"
        varchar version "模型版本"
        varchar provider "提供商"
        text description "模型描述"
        json api_config "API配置"
        timestamp created_at "创建时间"
        datetime deleted_at "删除时间"
    }
    
    evaluation_batches {
        int batch_id PK "评测批次ID"
        varchar name "批次名称"
        text description "批次描述"
        int model_id FK "模型ID"
        int judge_model_id FK "裁判模型ID"
        int version_id FK "数据集版本ID"
        enum evaluation_method "评测方法"
        timestamp start_time "开始时间"
        timestamp end_time "结束时间"
        enum status "状态"
        json metrics_summary "指标摘要"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }    
    %% ===========================================
    %% LLM答案和评测结果
    %% ===========================================
    llm_answers {
        int llm_answer_id PK "LLM答案ID"
        int model_id FK "模型ID"
        int standard_question_id FK "标准问题ID"
        int version_id FK "数据集版本ID"
        text content "答案内容"
        int latency "响应延迟ms"
        int tokens_used "使用Token数"
        text prompt_template "提示模板"
        json prompt_params "提示参数"
        decimal temperature "温度参数"
        decimal top_p "Top-p参数"
        int batch_id FK "批次ID"
        int retry_count "重试次数"
        boolean is_final "是否最终答案"
        int parent_answer_id FK "父答案ID"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
    }
    
    evaluations {
        int evaluation_id PK "评测ID"
        int llm_answer_id FK "LLM答案ID"
        int standard_answer_id FK "标准答案ID"
        decimal score "评分"
        varchar method "评测方法"
        json key_points_evaluation "关键点评测结果"
        int judge_model_id FK "裁判模型ID"
        int batch_id FK "批次ID"
        text comments "评测备注"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }
    
    evaluation_key_points {
        int id PK "评测关键点ID"
        int evaluation_id FK "评测ID"
        int key_point_id FK "关键点ID"
        enum status "评测状态"
        decimal score "关键点得分"
        timestamp created_at "创建时间"
    }
    
    evaluation_prompt_templates {
        int template_id PK "模板ID"
        varchar name "模板名称"
        text prompt_template "提示模板"
        enum template_type "模板类型"
        boolean is_active "是否激活"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }
    
    judge_tasks {
        int task_id PK "裁判任务ID"
        int batch_id FK "批次ID"
        int llm_answer_id FK "LLM答案ID"
        int standard_answer_id FK "标准答案ID"
        int judge_model_id FK "裁判模型ID"
        enum status "任务状态"
        decimal result_score "结果分数"
        text result_reason "评分理由"
        timestamp created_at "创建时间"
        timestamp completed_at "完成时间"
    }    
    %% ===========================================
    %% 众包任务管理
    %% ===========================================
    crowdsourcing_tasks {
        int task_id PK "众包任务ID"
        varchar title "任务标题"
        text description "任务描述"
        enum task_type "任务类型"
        int creator_id FK "创建者ID"
        int question_count "问题数量"
        int min_answers_per_question "每题最少答案数"
        text reward_info "奖励信息"
        timestamp start_time "开始时间"
        timestamp end_time "结束时间"
        enum status "任务状态"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
    }
    
    crowdsourced_answers {
        int answer_id PK "众包答案ID"
        int task_id FK "任务ID"
        int standard_question_id FK "标准问题ID"
        int user_id FK "用户ID"
        varchar contributor_name "贡献者姓名"
        varchar contributor_email "贡献者邮箱"
        varchar occupation "职业"
        varchar expertise "专业领域"
        text answer_text "答案文本"
        text references "参考资料"
        timestamp submission_time "提交时间"
        decimal quality_score "质量评分"
        enum review_status "审核状态"
        int reviewer_id FK "审核者ID"
        timestamp review_time "审核时间"
        text review_comment "审核意见"
        boolean is_selected "是否被选中"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
    }
    
    crowdsourcing_task_questions {
        int id PK "任务问题ID"
        int task_id FK "任务ID"
        int standard_question_id FK "标准问题ID"
        int current_answer_count "当前答案数"
        boolean is_completed "是否完成"
        timestamp created_at "创建时间"
    }
    
    %% ===========================================
    %% 答案评价和对比
    %% ===========================================
    answer_ratings {
        int rating_id PK "评分ID"
        int answer_id FK "答案ID"
        int rater_id FK "评分者ID"
        decimal score "评分"
        json rating_criteria "评分标准"
        text comment "评分备注"
        timestamp created_at "创建时间"
    }
    
    answer_comparisons {
        int comparison_id PK "对比ID"
        int standard_question_id FK "标准问题ID"
        int answer1_id FK "答案1 ID"
        int answer2_id FK "答案2 ID"
        int comparer_id FK "对比者ID"
        enum winner "获胜答案"
        text comparison_reason "对比理由"
        timestamp created_at "创建时间"
    }
    
    %% ===========================================
    %% 专家答案管理
    %% ===========================================
    expert_answers {
        int expert_answer_id PK "专家答案ID"
        int standard_question_id FK "标准问题ID"
        int expert_id FK "专家ID"
        text answer_text "答案文本"
        enum expertise_level "专家级别"
        timestamp submission_time "提交时间"
        boolean is_verified "是否已验证"
        int verifier_id FK "验证者ID"
        timestamp verification_time "验证时间"
        text verification_comment "验证意见"
        boolean is_selected_as_standard "是否被选为标准答案"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
    }    %% ===========================================
    %% 实体关系定义
    %% ===========================================
    
    %% 用户相关关系
    users ||--o{ standard_question_versions : "changed_by"
    users ||--o{ standard_answer_versions : "changed_by"
    users ||--o{ key_point_versions : "changed_by"
    users ||--o{ option_versions : "changed_by"
    users ||--o{ standard_answers : "selected_by"
    users ||--o{ crowdsourcing_tasks : "creator"
    users ||--o{ crowdsourced_answers : "contributor"
    users ||--o{ crowdsourced_answers : "reviewer"
    users ||--o{ answer_ratings : "rater"
    users ||--o{ answer_comparisons : "comparer"
    users ||--o{ expert_answers : "expert"
    users ||--o{ expert_answers : "verifier"
    
    %% 原始数据关系
    raw_questions ||--o{ raw_answers : "has"
    raw_questions ||--o{ standard_questions : "source_of"
    raw_questions ||--o{ standard_qa_pairs : "source_question"
    raw_answers ||--o{ standard_answers : "source_of"
    raw_answers ||--o{ standard_qa_pairs : "source_answer"
    
    %% 分类和标签关系
    question_categories ||--o{ standard_questions : "categorizes"
    question_categories ||--o{ question_categories : "parent_of"
    tags ||--o{ standard_question_tags : "used_in"
    
    %% 标准问题相关关系
    standard_questions ||--o{ standard_question_versions : "has_versions"
    standard_questions ||--o{ standard_question_tags : "has_tags"
    standard_questions ||--o{ standard_answers : "has_answers"
    standard_questions ||--o{ standard_qa_pairs : "in_qa_pairs"
    standard_questions ||--o{ objective_question_options : "has_options"
    standard_questions ||--o{ dataset_question_mapping : "in_datasets"
    standard_questions ||--o{ llm_answers : "answered_by_llm"
    standard_questions ||--o{ crowdsourced_answers : "crowdsourced"
    standard_questions ||--o{ crowdsourcing_task_questions : "in_tasks"
    standard_questions ||--o{ answer_comparisons : "compared"
    standard_questions ||--o{ expert_answers : "answered_by_expert"
    
    %% 标准答案相关关系
    standard_answers ||--o{ standard_answer_versions : "has_versions"
    standard_answers ||--o{ answer_key_points : "has_key_points"
    standard_answers ||--o{ standard_qa_pairs : "in_qa_pairs"
    standard_answers ||--o{ evaluations : "used_in_evaluations"
    standard_answers ||--o{ judge_tasks : "judged_against"
    
    %% 关键点和选项关系
    answer_key_points ||--o{ key_point_versions : "has_versions"
    answer_key_points ||--o{ evaluation_key_points : "evaluated"
    objective_question_options ||--o{ option_versions : "has_versions"
    
    %% 数据集相关关系
    dataset_versions ||--o{ dataset_question_mapping : "contains_questions"
    dataset_versions ||--o{ evaluation_batches : "used_in_batches"
    dataset_versions ||--o{ llm_answers : "contains_answers"
    
    %% LLM模型相关关系
    llm_models ||--o{ evaluation_batches : "evaluated_in"
    llm_models ||--o{ evaluation_batches : "judge_in"
    llm_models ||--o{ llm_answers : "generated"
    llm_models ||--o{ evaluations : "judges"
    llm_models ||--o{ judge_tasks : "acts_as_judge"
    
    %% 评测相关关系
    evaluation_batches ||--o{ llm_answers : "contains_answers"
    evaluation_batches ||--o{ evaluations : "has_evaluations"
    evaluation_batches ||--o{ judge_tasks : "has_judge_tasks"
    llm_answers ||--o{ llm_answers : "parent_of"
    llm_answers ||--o{ evaluations : "evaluated"
    llm_answers ||--o{ judge_tasks : "judged"
    evaluations ||--o{ evaluation_key_points : "has_key_point_results"
    
    %% 众包相关关系
    crowdsourcing_tasks ||--o{ crowdsourced_answers : "collects"
    crowdsourcing_tasks ||--o{ crowdsourcing_task_questions : "contains_questions"
    crowdsourced_answers ||--o{ answer_ratings : "rated"
    crowdsourced_answers ||--o{ answer_comparisons : "answer1"
    crowdsourced_answers ||--o{ answer_comparisons : "answer2"
```

