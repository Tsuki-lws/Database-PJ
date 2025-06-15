
```mermaid
%%{init: {
  'theme': 'base',
  'themeVariables': {
    'primaryColor': '#ffffff',
    'primaryTextColor': '#2d3748',
    'primaryBorderColor': '#4299e1',
    'lineColor': '#3182ce',
    'secondaryColor': '#f7fafc',
    'tertiaryColor': '#edf2f7',
    'background': '#f8fafc',
    'mainBkg': '#ffffff',
    'secondBkg': '#e2e8f0',
    'entityLabelColor': '#2d3748',
    'relationLabelColor': '#3182ce'
  },  'er': {
    'fontSize': 24,
    'entityPadding': 20,
    'entitySpacing': 200,
    'curve': 'cardinal',
    'entityColor': '#3182ce',
    'entityBkg': '#ebf8ff',
    'entityTextColor': '#2d3748',
    'relationColor': '#4299e1',
    'attributeColor': '#4a5568',
    'attributeBackgroundColorOdd': '#ffffff',
    'attributeBackgroundColorEven': '#f7fafc',
    'fillType0': '#ebf8ff',
    'fillType1': '#f0fff4',
    'fillType2': '#fef5e7',
    'fillType3': '#fef2f2',
    'fillType4': '#f3e8ff',
    'cScale0': '#3182ce',
    'cScale1': '#38a169',
    'cScale2': '#d69e2e',
    'cScale3': '#e53e3e',
    'cScale4': '#805ad5'
  }
}}%%

erDiagram    %% ===============================================
    %% 核心用户和权限管理模块
    %% ===============================================
    USERS {
        int user_id PK "用户唯一标识"
        varchar username "用户名"
        varchar password "加密密码"
        enum user_type "用户类型(管理员/专家/普通用户)"
        varchar email "邮箱地址"
        text profile "个人简介"
        text expertise_areas "专业领域"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间(软删除)"
    }    
    %% ===============================================
    %% 原始数据采集模块  
    %% ===============================================
    RAW_QUESTIONS {
        int question_id PK "原始问题ID"
        int source_question_id "源系统问题ID"
        text question_title "问题标题"
        text question_body "问题详细内容"
        varchar source "数据来源(StackOverflow/GitHub等)"
        int source_id "源系统内部ID"
        varchar source_url "原始链接地址"
        timestamp crawled_at "数据爬取时间"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
    }
    
    RAW_ANSWERS {
        int answer_id PK "原始答案ID"
        int source_answer_id "源系统答案ID"
        int question_id FK "关联的原始问题"
        text answer_body "答案详细内容"
        varchar author_info "作者信息"
        int upvotes "点赞数量"
        boolean is_accepted "是否被采纳为最佳答案"
        timestamp created_at "创建时间"
        timestamp updated_at "更新时间"
        datetime deleted_at "删除时间"
    }    
    %% ===============================================
    %% 分类和标签体系
    %% ===============================================
    QUESTION_CATEGORIES {
        int category_id PK "分类ID"
        varchar name "分类名称"
        text description "分类详细描述"
        int parent_id FK "父分类ID(支持多级分类)"
    }
    
    TAGS {
        int tag_id PK "标签ID"
        varchar tag_name "标签名称"
        text description "标签描述"
        varchar color "标签显示颜色"
    }
    
    %% ===============================================
    %% ⭐ 标准问题管理核心
    %% ===============================================
    STANDARD_QUESTIONS {
        int standard_question_id PK "🆔 标准问题ID"
        text question "❓ 标准化问题内容"
        int category_id FK "📚 所属分类"
        enum question_type "📝 问题类型(选择题/简答题/编程题)"
        enum difficulty "⚡ 难度等级(初级/中级/高级)"
        int source_question_id FK "🔗 来源原始问题"
        enum status "📊 状态(草稿/审核中/已发布)"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
        datetime deleted_at "🗑️ 删除时间"
        int version "📦 当前版本号"
    }
    
    STANDARD_QUESTION_VERSIONS {
        int version_id PK "🆔 版本记录ID"
        int standard_question_id FK "❓ 标准问题ID"
        text question "📝 该版本的问题内容"
        int category_id "📚 该版本的分类"
        enum question_type "📋 该版本的问题类型"
        enum difficulty "⚡ 该版本的难度"
        int version "📦 版本号"
        int changed_by FK "👤 修改者用户ID"
        text change_reason "📝 修改原因说明"
        timestamp created_at "⏰ 版本创建时间"
    }
    
    STANDARD_QUESTION_TAGS {
        int mapping_id PK "🆔 映射关系ID"
        int standard_question_id FK "❓ 标准问题ID"
        int tag_id FK "🏷️ 标签ID"
    }
    
    %% ===============================================
    %% 💯 标准答案管理系统
    %% ===============================================
    STANDARD_ANSWERS {
        int standard_answer_id PK "🆔 标准答案ID"
        int standard_question_id FK "❓ 对应的标准问题"
        text answer "📝 标准答案内容"
        int source_answer_id FK "🔗 来源原始答案"
        enum source_type "📊 来源类型(爬取/众包/专家)"
        int source_id "🔢 具体来源ID"
        text selection_reason "🎯 选择此答案的理由"
        int selected_by FK "👤 选择者用户ID"
        boolean is_final "✅ 是否为最终确定版本"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
        datetime deleted_at "🗑️ 删除时间"
        int version "📦 版本号"
    }
    
    STANDARD_ANSWER_VERSIONS {
        int version_id PK "🆔 答案版本ID"
        int standard_answer_id FK "💯 标准答案ID"
        text answer "📝 该版本的答案内容"
        int version "📦 版本号"
        int changed_by FK "👤 修改者用户ID"
        text change_reason "📝 修改原因"
        timestamp created_at "⏰ 版本创建时间"
    }
    
    %% ===============================================
    %% 🎯 答案关键点评分体系
    %% ===============================================
    ANSWER_KEY_POINTS {
        int key_point_id PK "🆔 关键点ID"
        int standard_answer_id FK "💯 所属标准答案"
        text point_text "🎯 关键点描述"
        int point_order "📊 关键点排序"
        decimal point_weight "⚖️ 权重(0-1)"
        enum point_type "📋 关键点类型(概念/方法/代码)"
        text example_text "📝 示例说明"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
        datetime deleted_at "🗑️ 删除时间"
        int version "📦 版本号"
    }
    
    KEY_POINT_VERSIONS {
        int version_id PK "🆔 关键点版本ID"
        int key_point_id FK "🎯 关键点ID"
        text point_text "📝 该版本的关键点描述"
        decimal point_weight "⚖️ 该版本的权重"
        enum point_type "📋 该版本的类型"
        text example_text "📝 该版本的示例"
        int version "📦 版本号"
        int changed_by FK "👤 修改者用户ID"
        text change_reason "📝 修改原因"
        timestamp created_at "⏰ 版本创建时间"
    }
    
    %% ===============================================
    %% 📝 选择题选项管理
    %% ===============================================
    OBJECTIVE_QUESTION_OPTIONS {
        int option_id PK "🆔 选项ID"
        int standard_question_id FK "❓ 所属标准问题"
        text option_text "📝 选项内容"
        varchar option_code "🔤 选项代码(A/B/C/D)"
        boolean is_correct "✅ 是否为正确答案"
        text explanation "📖 选项解释说明"
        int option_order "📊 选项显示顺序"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
        datetime deleted_at "🗑️ 删除时间"
        int version "📦 版本号"
    }
    
    OPTION_VERSIONS {
        int version_id PK "🆔 选项版本ID"
        int option_id FK "📝 选项ID"
        text option_text "📝 该版本的选项内容"
        varchar option_code "🔤 该版本的选项代码"
        boolean is_correct "✅ 该版本是否正确"
        text explanation "📖 该版本的解释"
        int version "📦 版本号"
        int changed_by FK "👤 修改者用户ID"
        text change_reason "📝 修改原因"
        timestamp created_at "⏰ 版本创建时间"
    }
    
    %% ===============================================
    %% 📊 数据集版本管理
    %% ===============================================
    STANDARD_QA_PAIRS {
        int qa_id PK "🆔 问答对ID"
        int standard_question_id FK "❓ 标准问题ID"
        int standard_answer_id FK "💯 标准答案ID"
        int source_question_id FK "🔗 源问题ID"
        int source_answer_id FK "🔗 源答案ID"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
    }
    
    DATASET_VERSIONS {
        int version_id PK "🆔 数据集版本ID"
        varchar name "📦 数据集名称"
        text description "📖 数据集描述"
        date release_date "📅 发布日期"
        boolean is_published "🚀 是否已发布"
        int question_count "📊 包含问题数量"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
    }
    
    DATASET_QUESTION_MAPPING {
        int mapping_id PK "🆔 映射ID"
        int version_id FK "📦 数据集版本ID"
        int standard_question_id FK "❓ 标准问题ID"
    }
    
    %% ===============================================
    %% 🤖 LLM模型管理
    %% ===============================================
    LLM_MODELS {
        int model_id PK "🆔 模型ID"
        varchar name "🤖 模型名称"
        varchar version "📦 模型版本"
        varchar provider "🏢 提供商"
        text description "📖 模型描述"
        json api_config "⚙️ API配置信息"
        timestamp created_at "⏰ 创建时间"
        datetime deleted_at "🗑️ 删除时间"
    }
    
    EVALUATION_BATCHES {
        int batch_id PK "🆔 评测批次ID"
        varchar name "📋 批次名称"
        text description "📖 批次描述"
        int model_id FK "🤖 被评测模型ID"
        int judge_model_id FK "⚖️ 裁判模型ID"
        int version_id FK "📦 使用的数据集版本"
        enum evaluation_method "📏 评测方法"
        timestamp start_time "🚀 开始时间"
        timestamp end_time "🏁 结束时间"
        enum status "📊 批次状态"
        json metrics_summary "📈 评测指标摘要"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
    }
    
    %% ===============================================
    %% 🎯 LLM答案和评测结果
    %% ===============================================
    LLM_ANSWERS {
        int llm_answer_id PK "🆔 LLM答案ID"
        int model_id FK "🤖 生成模型ID"
        int standard_question_id FK "❓ 回答的标准问题"
        int version_id FK "📦 使用的数据集版本"
        text content "📝 生成的答案内容"
        int latency "⏱️ 响应延迟(毫秒)"
        int tokens_used "🔢 使用的Token数量"
        text prompt_template "📋 使用的提示模板"
        json prompt_params "⚙️ 提示参数"
        decimal temperature "🌡️ 温度参数"
        decimal top_p "🎯 Top-p参数"
        int batch_id FK "📊 所属评测批次"
        int retry_count "🔄 重试次数"
        boolean is_final "✅ 是否为最终答案"
        int parent_answer_id FK "👨‍👦 父答案ID(重试关系)"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
        datetime deleted_at "🗑️ 删除时间"
    }
    
    EVALUATIONS {
        int evaluation_id PK "🆔 评测记录ID"
        int llm_answer_id FK "🤖 被评测的LLM答案"
        int standard_answer_id FK "💯 对比的标准答案"
        decimal score "📊 总体评分(0-100)"
        varchar method "📏 评测方法"
        json key_points_evaluation "🎯 关键点评测详情"
        int judge_model_id FK "⚖️ 裁判模型ID"
        int batch_id FK "📊 所属评测批次"
        text comments "📝 评测备注说明"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
    }
    
    EVALUATION_KEY_POINTS {
        int id PK "🆔 关键点评测ID"
        int evaluation_id FK "📊 所属评测记录"
        int key_point_id FK "🎯 被评测关键点"
        enum status "✅ 评测状态(命中/未命中/部分命中)"
        decimal score "📊 关键点得分"
        timestamp created_at "⏰ 创建时间"
    }
    
    %% ===============================================
    %% 🎪 众包任务管理
    %% ===============================================
    CROWDSOURCING_TASKS {
        int task_id PK "🆔 众包任务ID"
        varchar title "📋 任务标题"
        text description "📖 任务详细描述"
        enum task_type "📝 任务类型"
        int creator_id FK "👤 任务创建者"
        int question_count "📊 包含问题数量"
        int min_answers_per_question "📈 每题最少答案数"
        text reward_info "🎁 奖励信息"
        timestamp start_time "🚀 任务开始时间"
        timestamp end_time "🏁 任务结束时间"
        enum status "📊 任务状态"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
    }
    
    CROWDSOURCED_ANSWERS {
        int answer_id PK "🆔 众包答案ID"
        int task_id FK "🎪 所属任务"
        int standard_question_id FK "❓ 回答的问题"
        int user_id FK "👤 贡献者用户ID"
        varchar contributor_name "👨‍💻 贡献者姓名"
        varchar contributor_email "📧 贡献者邮箱"
        varchar occupation "💼 职业"
        varchar expertise "🎯 专业领域"
        text answer_text "📝 答案内容"
        text references "📚 参考资料"
        timestamp submission_time "📤 提交时间"
        decimal quality_score "⭐ 质量评分"
        enum review_status "📋 审核状态"
        int reviewer_id FK "👨‍⚖️ 审核者ID"
        timestamp review_time "⏰ 审核时间"
        text review_comment "📝 审核意见"
        boolean is_selected "✅ 是否被选中"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
        datetime deleted_at "🗑️ 删除时间"
    }
    
    %% ===============================================
    %% 👨‍🎓 专家答案管理
    %% ===============================================
    EXPERT_ANSWERS {
        int expert_answer_id PK "🆔 专家答案ID"
        int standard_question_id FK "❓ 回答的标准问题"
        int expert_id FK "👨‍🎓 专家用户ID"
        text answer_text "📝 专家答案内容"
        enum expertise_level "🌟 专家级别"
        timestamp submission_time "📤 提交时间"
        boolean is_verified "✅ 是否已验证"
        int verifier_id FK "👨‍⚖️ 验证者ID"
        timestamp verification_time "⏰ 验证时间"
        text verification_comment "📝 验证意见"
        boolean is_selected_as_standard "⭐ 是否被选为标准答案"
        timestamp created_at "⏰ 创建时间"
        timestamp updated_at "🔄 更新时间"
        datetime deleted_at "🗑️ 删除时间"
    }    %% ===============================================
    %% 📊 实体关系定义 - 数据流与依赖关系图
    %% ===============================================
    
    %% 👤 核心用户管理关系
    USERS ||--o{ STANDARD_QUESTION_VERSIONS : "创建问题版本"
    USERS ||--o{ STANDARD_ANSWER_VERSIONS : "修订答案版本"
    USERS ||--o{ KEY_POINT_VERSIONS : "维护关键点版本"
    USERS ||--o{ OPTION_VERSIONS : "编辑选项版本"
    USERS ||--o{ STANDARD_ANSWERS : "审核标准答案"
    USERS ||--o{ CROWDSOURCING_TASKS : "发起众包任务"
    USERS ||--o{ CROWDSOURCED_ANSWERS : "贡献众包答案"
    USERS ||--o{ CROWDSOURCED_ANSWERS : "审核众包提交"
    USERS ||--o{ EXPERT_ANSWERS : "提供专家答案"
    USERS ||--o{ EXPERT_ANSWERS : "验证专家内容"
    
    %% 📊 原始数据处理流程
    RAW_QUESTIONS ||--o{ RAW_ANSWERS : "问题对应答案"
    RAW_QUESTIONS ||--o{ STANDARD_QUESTIONS : "转化为标准问题"
    RAW_QUESTIONS ||--o{ STANDARD_QA_PAIRS : "形成问答对源"
    RAW_ANSWERS ||--o{ STANDARD_ANSWERS : "生成标准答案"
    RAW_ANSWERS ||--o{ STANDARD_QA_PAIRS : "构建答案源"
    
    %% 📚 知识组织体系
    QUESTION_CATEGORIES ||--o{ STANDARD_QUESTIONS : "分类管理问题"
    QUESTION_CATEGORIES ||--o{ QUESTION_CATEGORIES : "构建分类树"
    TAGS ||--o{ STANDARD_QUESTION_TAGS : "标签化问题"
    
    %% ⭐ 标准化内容管理核心
    STANDARD_QUESTIONS ||--o{ STANDARD_QUESTION_VERSIONS : "版本历史追踪"
    STANDARD_QUESTIONS ||--o{ STANDARD_QUESTION_TAGS : "多维度标签"
    STANDARD_QUESTIONS ||--o{ STANDARD_ANSWERS : "配对标准答案"
    STANDARD_QUESTIONS ||--o{ STANDARD_QA_PAIRS : "形成问答集"
    STANDARD_QUESTIONS ||--o{ OBJECTIVE_QUESTION_OPTIONS : "配置选择项"
    STANDARD_QUESTIONS ||--o{ DATASET_QUESTION_MAPPING : "纳入数据集"
    STANDARD_QUESTIONS ||--o{ LLM_ANSWERS : "生成模型回答"
    STANDARD_QUESTIONS ||--o{ CROWDSOURCED_ANSWERS : "收集群智答案"
    STANDARD_QUESTIONS ||--o{ EXPERT_ANSWERS : "获取专家见解"    
    %% 💯 答案质量保证体系
    STANDARD_ANSWERS ||--o{ STANDARD_ANSWER_VERSIONS : "答案迭代优化"
    STANDARD_ANSWERS ||--o{ ANSWER_KEY_POINTS : "关键点分解"
    STANDARD_ANSWERS ||--o{ STANDARD_QA_PAIRS : "问答集构建"
    STANDARD_ANSWERS ||--o{ EVALUATIONS : "评测基准设定"
    
    %% 🎯 细粒度评价机制
    ANSWER_KEY_POINTS ||--o{ KEY_POINT_VERSIONS : "关键点演进"
    ANSWER_KEY_POINTS ||--o{ EVALUATION_KEY_POINTS : "精准评测"
    OBJECTIVE_QUESTION_OPTIONS ||--o{ OPTION_VERSIONS : "选项完善"
    
    %% 📦 数据集生命周期管理
    DATASET_VERSIONS ||--o{ DATASET_QUESTION_MAPPING : "版本内容映射"
    DATASET_VERSIONS ||--o{ EVALUATION_BATCHES : "评测基础数据"
    DATASET_VERSIONS ||--o{ LLM_ANSWERS : "模型测试依据"
    
    %% 🤖 AI模型评测生态
    LLM_MODELS ||--o{ EVALUATION_BATCHES : "模型性能测试"
    LLM_MODELS ||--o{ EVALUATION_BATCHES : "智能评判系统"
    LLM_MODELS ||--o{ LLM_ANSWERS : "自动化回答"
    LLM_MODELS ||--o{ EVALUATIONS : "客观评价"
    
    %% 📊 评测数据分析链路
    EVALUATION_BATCHES ||--o{ LLM_ANSWERS : "批量测试管理"
    EVALUATION_BATCHES ||--o{ EVALUATIONS : "系统性评测"
    LLM_ANSWERS ||--o{ LLM_ANSWERS : "答案优化迭代"
    LLM_ANSWERS ||--o{ EVALUATIONS : "单答案评测"
    EVALUATIONS ||--o{ EVALUATION_KEY_POINTS : "多维度分析"
    
    %% 🎪 众包协作生态系统
    CROWDSOURCING_TASKS ||--o{ CROWDSOURCED_ANSWERS : "任务执行"
    CROWDSOURCED_ANSWERS ||--o{ CROWDSOURCED_ANSWERS : "答案对比优化"
```

