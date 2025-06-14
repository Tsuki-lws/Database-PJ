-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 原始问题表
CREATE TABLE raw_questions (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    source_question_id INT COMMENT '来源平台的问题ID',
    question_title TEXT COMMENT '问题标题',
    question_body TEXT NOT NULL COMMENT '问题内容(HTML格式)',
    source VARCHAR(255) COMMENT '来源网站',
    source_id INT COMMENT '来源网站上的ID',
    source_url VARCHAR(1024) COMMENT '来源URL',
    crawled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '抓取时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除'
) COMMENT='存储从问答社区收集的原始问题';

-- 原始回答表
CREATE TABLE raw_answers (
    answer_id INT AUTO_INCREMENT PRIMARY KEY,
    source_answer_id INT COMMENT '来源平台的回答ID',
    question_id INT NOT NULL,
    answer_body TEXT NOT NULL COMMENT '回答内容(HTML格式)',
    author_info VARCHAR(255) COMMENT '回答者信息',
    upvotes INT DEFAULT 0 COMMENT '点赞数量',
    is_accepted BOOLEAN DEFAULT FALSE COMMENT '是否为采纳的答案',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除',
    FOREIGN KEY (question_id) REFERENCES raw_questions(question_id) ON DELETE CASCADE
) COMMENT='存储原始问题的回答';

-- 问题分类表
CREATE TABLE question_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id INT,
    FOREIGN KEY (parent_id) REFERENCES question_categories(category_id) ON DELETE SET NULL
) COMMENT='问题分类体系';

-- 标签表
CREATE TABLE tags (
    tag_id INT AUTO_INCREMENT PRIMARY KEY,
    tag_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    color VARCHAR(50)
) COMMENT='问题标签';

-- 用户表（优先创建，被许多表引用）
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_type ENUM('admin', 'regular', 'expert', 'reviewer') DEFAULT 'regular' COMMENT '用户类型',
    email VARCHAR(255),
    profile TEXT COMMENT '用户简介',
    expertise_areas TEXT COMMENT '专业领域',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记'
) COMMENT='用户表';

-- 标准问题表
CREATE TABLE standard_questions (
    standard_question_id INT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL COMMENT '标准化后的问题文本',
    category_id INT,
    question_type ENUM('single_choice', 'multiple_choice', 'simple_fact', 'subjective') DEFAULT 'subjective' COMMENT '问题类型',
    difficulty ENUM('easy', 'medium', 'hard') COMMENT '问题难度',
    source_question_id INT COMMENT '关联的原始问题ID',
    status ENUM('draft', 'pending_review', 'approved', 'rejected') DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除',
    version INT DEFAULT 1 COMMENT '问题的当前版本号',
    FOREIGN KEY (category_id) REFERENCES question_categories(category_id) ON DELETE SET NULL,
    FOREIGN KEY (source_question_id) REFERENCES raw_questions(question_id) ON DELETE SET NULL
) COMMENT='经过标准化处理的问题';

-- 标准问题历史版本表
CREATE TABLE standard_question_versions (
    version_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_question_id INT NOT NULL COMMENT '关联的标准问题ID',
    question TEXT NOT NULL COMMENT '问题文本的历史版本',
    category_id INT COMMENT '历史版本的分类ID',
    question_type ENUM('single_choice', 'multiple_choice', 'simple_fact', 'subjective') COMMENT '历史版本的问题类型',
    difficulty ENUM('easy', 'medium', 'hard') COMMENT '历史版本的问题难度',
    version INT NOT NULL COMMENT '版本号',
    changed_by INT COMMENT '修改人ID',
    change_reason TEXT COMMENT '修改原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '该版本的创建时间',
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE KEY (standard_question_id, version)
) COMMENT='标准问题的历史版本记录';

-- 标准问题标签关联表
CREATE TABLE standard_question_tags (
    mapping_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_question_id INT NOT NULL,
    tag_id INT NOT NULL,
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE,
    UNIQUE KEY (standard_question_id, tag_id)
) COMMENT='问题与标签的多对多关系';

-- 众包任务表（提前创建，解决循环引用问题）
CREATE TABLE crowdsourcing_tasks (
                                     task_id INT AUTO_INCREMENT PRIMARY KEY,
                                     title VARCHAR(255) NOT NULL COMMENT '任务标题',
                                     description TEXT COMMENT '任务描述',
                                     task_type ENUM('answer_collection', 'answer_review', 'answer_rating') DEFAULT 'answer_collection' COMMENT '任务类型',
                                     creator_id INT NOT NULL COMMENT '创建人ID',
                                     question_count INT DEFAULT 0 COMMENT '问题数量',
                                     min_answers_per_question INT DEFAULT 1 COMMENT '每个问题最少需要的答案数',
                                     reward_info TEXT COMMENT '奖励信息',
                                     start_time TIMESTAMP NULL COMMENT '开始时间',
                                     end_time TIMESTAMP NULL COMMENT '结束时间',
                                     status ENUM('DRAFT', 'PUBLISHED', 'COMPLETED', 'CLOSED') DEFAULT 'DRAFT',
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     FOREIGN KEY (creator_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='众包任务管理';

-- 众包候选答案表
CREATE TABLE crowdsourced_answers (
                                      answer_id INT AUTO_INCREMENT PRIMARY KEY,
                                      task_id INT COMMENT '关联的众包任务ID',
                                      standard_question_id INT NOT NULL,
                                      user_id INT COMMENT '提交答案的用户ID，可以为NULL表示匿名提交',
                                      contributor_name VARCHAR(255) COMMENT '提交人名称',
                                      contributor_email VARCHAR(255) COMMENT '提交人邮箱',
                                      occupation VARCHAR(100) COMMENT '职业/身份',
                                      expertise VARCHAR(255) COMMENT '专业领域',
                                      answer_text TEXT NOT NULL COMMENT '候选答案文本',
                                      `references` TEXT COMMENT '参考资料',
                                      submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
                                      quality_score DECIMAL(3,1) DEFAULT NULL COMMENT '质量评分(0-10)',
                                      review_status ENUM('SUBMITTED', 'APPROVED', 'REJECTED', 'PROMOTED') DEFAULT 'SUBMITTED' COMMENT '审核状态',
                                      reviewer_id INT COMMENT '审核人ID',
                                      review_time TIMESTAMP NULL COMMENT '审核时间',
                                      review_comment TEXT COMMENT '审核意见',
                                      is_selected BOOLEAN DEFAULT FALSE COMMENT '是否被选为标准答案',
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除',
                                      FOREIGN KEY (task_id) REFERENCES crowdsourcing_tasks(task_id) ON DELETE SET NULL,
                                      FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
                                      FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
                                      FOREIGN KEY (reviewer_id) REFERENCES users(user_id) ON DELETE SET NULL
) COMMENT='众包收集的候选答案';

-- 专家回答表
CREATE TABLE expert_answers (
    expert_answer_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_question_id INT NOT NULL,
    expert_id INT NOT NULL COMMENT '专家用户ID',
    answer_text TEXT NOT NULL COMMENT '专家提供的答案',
    expertise_level ENUM('junior', 'senior', 'authority') DEFAULT 'junior' COMMENT '专家级别',
    submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    is_verified BOOLEAN DEFAULT FALSE COMMENT '是否经过验证',
    verifier_id INT COMMENT '验证人ID',
    verification_time TIMESTAMP NULL COMMENT '验证时间',
    verification_comment TEXT COMMENT '验证意见',
    is_selected_as_standard BOOLEAN DEFAULT FALSE COMMENT '是否被选为标准答案',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除',
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    FOREIGN KEY (expert_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (verifier_id) REFERENCES users(user_id) ON DELETE SET NULL
) COMMENT='专家提供的高质量答案';

-- 标准答案表
CREATE TABLE standard_answers (
    standard_answer_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_question_id INT NOT NULL,
    answer TEXT NOT NULL COMMENT '标准答案内容',
    source_answer_id INT COMMENT '关联的原始回答ID',
    source_type ENUM('raw', 'crowdsourced', 'expert', 'manual') DEFAULT 'manual' COMMENT '标准答案来源类型',
    source_id INT COMMENT '来源ID，根据source_type对应不同表的ID',
    selection_reason TEXT COMMENT '选择该答案作为标准答案的原因',
    selected_by INT COMMENT '选择人ID',
    is_final BOOLEAN DEFAULT FALSE COMMENT '是否为最终确认的标准答案',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除',
    version INT DEFAULT 1 COMMENT '答案的当前版本号',
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    FOREIGN KEY (source_answer_id) REFERENCES raw_answers(answer_id) ON DELETE SET NULL,
    FOREIGN KEY (selected_by) REFERENCES users(user_id) ON DELETE SET NULL
) COMMENT='标准问题的标准答案';

-- 标准答案历史版本表
CREATE TABLE standard_answer_versions (
    version_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_answer_id INT NOT NULL COMMENT '关联的标准答案ID',
    answer TEXT NOT NULL COMMENT '答案内容的历史版本',
    version INT NOT NULL COMMENT '版本号',
    changed_by INT COMMENT '修改人ID',
    change_reason TEXT COMMENT '修改原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '该版本的创建时间',
    FOREIGN KEY (standard_answer_id) REFERENCES standard_answers(standard_answer_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE KEY (standard_answer_id, version)
) COMMENT='标准答案的历史版本记录';

-- 标准答案关键点表（主观题的关键评分点）
CREATE TABLE answer_key_points (
    key_point_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_answer_id INT NOT NULL,
    point_text TEXT NOT NULL COMMENT '关键点描述',
    point_order INT NOT NULL DEFAULT 0 COMMENT '关键点顺序',
    point_weight DECIMAL(5,2) DEFAULT 1.0 COMMENT '评分权重(1.0表示标准权重)',
    point_type ENUM('required', 'bonus', 'penalty') DEFAULT 'required' COMMENT '评分点类型：必须、加分、减分',
    example_text TEXT COMMENT '示例内容，用于说明该评分点的应用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除',
    version INT DEFAULT 1 COMMENT '关键点的当前版本号',
    FOREIGN KEY (standard_answer_id) REFERENCES standard_answers(standard_answer_id) ON DELETE CASCADE
) COMMENT='标准答案的关键点/评分点';

-- 标准回答关键点历史版本表
CREATE TABLE key_point_versions (
    version_id INT AUTO_INCREMENT PRIMARY KEY,
    key_point_id INT NOT NULL COMMENT '关联的关键点ID',
    point_text TEXT NOT NULL COMMENT '关键点描述的历史版本',
    point_weight DECIMAL(5,2) COMMENT '历史版本的评分权重',
    point_type ENUM('required', 'bonus', 'penalty') COMMENT '历史版本的评分点类型',
    example_text TEXT COMMENT '历史版本的示例内容',
    version INT NOT NULL COMMENT '版本号',
    changed_by INT COMMENT '修改人ID',
    change_reason TEXT COMMENT '修改原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '该版本的创建时间',
    FOREIGN KEY (key_point_id) REFERENCES answer_key_points(key_point_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE KEY (key_point_id, version)
) COMMENT='标准回答关键点的历史版本记录';

-- 客观题选项表
CREATE TABLE objective_question_options (
    option_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_question_id INT NOT NULL,    
    option_text TEXT NOT NULL COMMENT '选项文本',
    option_code VARCHAR(10) NOT NULL COMMENT '选项代码，如A、B、C等',
    is_correct BOOLEAN DEFAULT FALSE COMMENT '是否为正确答案',
    explanation TEXT COMMENT '选项解释',
    option_order INT NOT NULL DEFAULT 0 COMMENT '选项顺序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除',
    version INT DEFAULT 1 COMMENT '选项的当前版本号',
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE
) COMMENT='客观题的选项';

-- 客观题选项历史版本表
CREATE TABLE option_versions (
    version_id INT AUTO_INCREMENT PRIMARY KEY,
    option_id INT NOT NULL COMMENT '关联的选项ID',
    option_text TEXT NOT NULL COMMENT '选项文本的历史版本',
    option_code VARCHAR(10) NOT NULL COMMENT '历史版本的选项代码',
    is_correct BOOLEAN COMMENT '历史版本的正确性标记',
    explanation TEXT COMMENT '历史版本的选项解释',
    version INT NOT NULL COMMENT '版本号',
    changed_by INT COMMENT '修改人ID',
    change_reason TEXT COMMENT '修改原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '该版本的创建时间',
    FOREIGN KEY (option_id) REFERENCES objective_question_options(option_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    UNIQUE KEY (option_id, version)
) COMMENT='客观题选项的历史版本记录';

-- 标准问答对表
CREATE TABLE standard_qa_pairs (
    qa_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_question_id INT NOT NULL,
    standard_answer_id INT NOT NULL,
    source_question_id INT COMMENT '原始问题ID',
    source_answer_id INT COMMENT '原始回答ID', 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    FOREIGN KEY (standard_answer_id) REFERENCES standard_answers(standard_answer_id) ON DELETE CASCADE,
    FOREIGN KEY (source_question_id) REFERENCES raw_questions(question_id) ON DELETE SET NULL,
    FOREIGN KEY (source_answer_id) REFERENCES raw_answers(answer_id) ON DELETE SET NULL
) COMMENT='标准问答对映射';

-- 数据集版本表
CREATE TABLE dataset_versions (
    version_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    release_date DATE,
    is_published BOOLEAN DEFAULT FALSE COMMENT '是否已发布',
    question_count INT DEFAULT 0 COMMENT '包含问题数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='数据集版本管理';

-- 数据集问题映射表（哪个数据集版本包含哪些标准问题）
CREATE TABLE dataset_question_mapping (
    mapping_id INT AUTO_INCREMENT PRIMARY KEY,
    version_id INT NOT NULL,
    standard_question_id INT NOT NULL,
    FOREIGN KEY (version_id) REFERENCES dataset_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    UNIQUE KEY (version_id, standard_question_id)
) COMMENT='数据集与问题的多对多关系';

-- LLM模型表
CREATE TABLE llm_models (
    model_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    version VARCHAR(50) NOT NULL,
    provider VARCHAR(100),
    description TEXT,
    api_config JSON COMMENT '模型API配置信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (name, version)
) COMMENT='大语言模型信息';

-- 评测批次表（简化版）
CREATE TABLE evaluation_batches (
    batch_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    model_id INT NOT NULL,
    judge_model_id INT COMMENT '裁判模型ID',
    version_id INT NOT NULL COMMENT '数据集版本',
    evaluation_method ENUM('human', 'auto', 'judge_model') NOT NULL DEFAULT 'auto',
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    status ENUM('pending', 'in_progress', 'completed', 'failed') DEFAULT 'pending',
    metrics_summary JSON COMMENT '批次评测结果汇总',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES llm_models(model_id) ON DELETE CASCADE,
    FOREIGN KEY (judge_model_id) REFERENCES llm_models(model_id) ON DELETE SET NULL,
    FOREIGN KEY (version_id) REFERENCES dataset_versions(version_id) ON DELETE CASCADE
) COMMENT='模型评测批次管理';

-- LLM回答表
CREATE TABLE llm_answers (
    llm_answer_id INT AUTO_INCREMENT PRIMARY KEY,
    model_id INT NOT NULL,
    standard_question_id INT NOT NULL,
    version_id INT NOT NULL COMMENT '数据集版本ID',
    content TEXT NOT NULL COMMENT '模型生成的回答',
    latency INT COMMENT '响应时间(ms)',
    tokens_used INT COMMENT '使用的tokens数量',
    prompt_template TEXT COMMENT '使用的提示模板',
    prompt_params JSON COMMENT '提示模板参数',
    temperature DECIMAL(4,2) COMMENT '温度参数',
    top_p DECIMAL(4,2) COMMENT 'top-p参数',
    batch_id INT COMMENT '评测批次ID',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    is_final BOOLEAN DEFAULT TRUE COMMENT '是否为最终版本',
    parent_answer_id INT COMMENT '上一个版本的回答ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL COMMENT '软删除标记，非空表示已删除',
    FOREIGN KEY (model_id) REFERENCES llm_models(model_id) ON DELETE CASCADE,
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    FOREIGN KEY (version_id) REFERENCES dataset_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES evaluation_batches(batch_id) ON DELETE SET NULL,
    FOREIGN KEY (parent_answer_id) REFERENCES llm_answers(llm_answer_id) ON DELETE SET NULL
) COMMENT='模型生成的回答';

-- 评测结果表（简化版）
CREATE TABLE evaluations (
    evaluation_id INT AUTO_INCREMENT PRIMARY KEY,
    llm_answer_id INT NOT NULL,
    standard_answer_id INT NOT NULL COMMENT '参考的标准答案',
    score DECIMAL(5,2) COMMENT '综合评分',
    method VARCHAR(50) COMMENT '评测方法(human/auto/judge)',
    key_points_evaluation JSON COMMENT '关键点评估结果，如 {"missed":[1,3], "partial":[2], "matched":[4,5]}',
    judge_model_id INT COMMENT '裁判模型ID（如果是模型评测）',
    batch_id INT COMMENT '关联的评测批次',
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (llm_answer_id) REFERENCES llm_answers(llm_answer_id) ON DELETE CASCADE,
    FOREIGN KEY (standard_answer_id) REFERENCES standard_answers(standard_answer_id) ON DELETE CASCADE,
    FOREIGN KEY (judge_model_id) REFERENCES llm_models(model_id) ON DELETE SET NULL,
    FOREIGN KEY (batch_id) REFERENCES evaluation_batches(batch_id) ON DELETE SET NULL
) COMMENT='模型回答的评测结果';

-- 评测关键点详情表（简化版）
CREATE TABLE evaluation_key_points (
    id INT AUTO_INCREMENT PRIMARY KEY,
    evaluation_id INT NOT NULL,
    key_point_id INT NOT NULL,
    status ENUM('missed', 'partial', 'matched') NOT NULL,
    score DECIMAL(5,2) COMMENT '该关键点的得分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evaluation_id) REFERENCES evaluations(evaluation_id) ON DELETE CASCADE,
    FOREIGN KEY (key_point_id) REFERENCES answer_key_points(key_point_id) ON DELETE CASCADE
) COMMENT='评测关键点结果';

-- 评测提示模板表（简化版）
CREATE TABLE evaluation_prompt_templates (
    template_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    prompt_template TEXT NOT NULL COMMENT '评测提示模板文本',
    template_type ENUM('judge', 'comparison') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否为活跃模板',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='评测提示模板';

-- 裁判任务表
CREATE TABLE judge_tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    batch_id INT NOT NULL COMMENT '关联的评测批次',
    llm_answer_id INT NOT NULL COMMENT '待评测的LLM回答',
    standard_answer_id INT NOT NULL COMMENT '标准答案',
    judge_model_id INT NOT NULL COMMENT '裁判模型',
    status ENUM('pending', 'in_progress', 'completed', 'failed') DEFAULT 'pending',
    result_score DECIMAL(5,2) COMMENT '评分结果',
    result_reason TEXT COMMENT '评分理由',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    FOREIGN KEY (batch_id) REFERENCES evaluation_batches(batch_id) ON DELETE CASCADE,
    FOREIGN KEY (llm_answer_id) REFERENCES llm_answers(llm_answer_id) ON DELETE CASCADE,
    FOREIGN KEY (standard_answer_id) REFERENCES standard_answers(standard_answer_id) ON DELETE CASCADE,
    FOREIGN KEY (judge_model_id) REFERENCES llm_models(model_id) ON DELETE CASCADE
) COMMENT='裁判模型评测任务';

-- 众包任务-问题关联表
CREATE TABLE crowdsourcing_task_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    standard_question_id INT NOT NULL,
    current_answer_count INT DEFAULT 0 COMMENT '当前收集的答案数量',
    is_completed BOOLEAN DEFAULT FALSE COMMENT '是否已收集足够答案',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES crowdsourcing_tasks(task_id) ON DELETE CASCADE,
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    UNIQUE KEY (task_id, standard_question_id)
) COMMENT='众包任务与问题的关联';

-- 众包答案评分表
CREATE TABLE answer_ratings (
    rating_id INT AUTO_INCREMENT PRIMARY KEY,
    answer_id INT NOT NULL COMMENT '被评分的答案ID',
    rater_id INT NOT NULL COMMENT '评分人ID',
    score DECIMAL(3,1) NOT NULL COMMENT '评分(0-10)',
    rating_criteria JSON COMMENT '评分标准和对应分数, 如{"accuracy": 8, "completeness": 7, "clarity": 9}',
    comment TEXT COMMENT '评分说明',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (answer_id) REFERENCES crowdsourced_answers(answer_id) ON DELETE CASCADE,
    FOREIGN KEY (rater_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY (answer_id, rater_id)
) COMMENT='众包答案的评分记录';

-- 答案比较表
CREATE TABLE answer_comparisons (
    comparison_id INT AUTO_INCREMENT PRIMARY KEY,
    standard_question_id INT NOT NULL COMMENT '关联的标准问题',
    answer1_id INT NOT NULL COMMENT '第一个答案ID',
    answer2_id INT NOT NULL COMMENT '第二个答案ID',
    comparer_id INT NOT NULL COMMENT '比较者ID',
    winner ENUM('answer1', 'answer2', 'tie') NOT NULL COMMENT '选择的胜出方',
    comparison_reason TEXT COMMENT '比较理由',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (standard_question_id) REFERENCES standard_questions(standard_question_id) ON DELETE CASCADE,
    FOREIGN KEY (answer1_id) REFERENCES crowdsourced_answers(answer_id) ON DELETE CASCADE,
    FOREIGN KEY (answer2_id) REFERENCES crowdsourced_answers(answer_id) ON DELETE CASCADE,
    FOREIGN KEY (comparer_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT='众包答案的两两比较';

-- 创建额外的索引以优化查询性能
CREATE INDEX idx_dataset_version_mapping ON dataset_question_mapping(version_id, standard_question_id);
CREATE INDEX idx_source_question_ids ON raw_questions(source_question_id);
CREATE INDEX idx_source_answer_ids ON raw_answers(source_answer_id);
CREATE INDEX idx_standard_qa_pairs ON standard_qa_pairs(standard_question_id, standard_answer_id);
CREATE INDEX idx_qa_source_ids ON standard_qa_pairs(source_question_id, source_answer_id);
CREATE INDEX idx_key_points ON answer_key_points(standard_answer_id);

-- 创建索引以优化查询性能（简化版）
CREATE INDEX idx_raw_questions_source ON raw_questions(source);
CREATE INDEX idx_raw_questions_deleted ON raw_questions(deleted_at);
CREATE INDEX idx_standard_questions_category ON standard_questions(category_id);
CREATE INDEX idx_standard_questions_status ON standard_questions(status);
CREATE INDEX idx_standard_questions_type ON standard_questions(question_type);
CREATE INDEX idx_standard_questions_deleted ON standard_questions(deleted_at);
CREATE INDEX idx_standard_answers_deleted ON standard_answers(deleted_at);
CREATE INDEX idx_standard_answers_question ON standard_answers(standard_question_id);
CREATE INDEX idx_standard_answers_final ON standard_answers(is_final);
CREATE INDEX idx_llm_answers_model ON llm_answers(model_id);
CREATE INDEX idx_llm_answers_question ON llm_answers(standard_question_id);
CREATE INDEX idx_llm_answers_batch ON llm_answers(batch_id);
CREATE INDEX idx_llm_answers_deleted ON llm_answers(deleted_at);
CREATE INDEX idx_evaluations_score ON evaluations(score);
CREATE INDEX idx_evaluations_answer ON evaluations(llm_answer_id);
CREATE INDEX idx_evaluations_method ON evaluations(method);
CREATE INDEX idx_evaluations_key_points ON evaluation_key_points(evaluation_id);
CREATE INDEX idx_crowdsourced_answers_question ON crowdsourced_answers(standard_question_id);
CREATE INDEX idx_crowdsourced_answers_task ON crowdsourced_answers(task_id);
CREATE INDEX idx_crowdsourced_answers_status ON crowdsourced_answers(review_status);
CREATE INDEX idx_crowdsourcing_tasks_status ON crowdsourcing_tasks(status);
CREATE INDEX idx_expert_answers_question ON expert_answers(standard_question_id);
CREATE INDEX idx_judge_tasks_status ON judge_tasks(status);
CREATE INDEX idx_judge_tasks_batch ON judge_tasks(batch_id);
CREATE INDEX idx_objective_options_question ON objective_question_options(standard_question_id);

-- 添加版本历史索引
CREATE INDEX idx_standard_question_versions ON standard_question_versions(standard_question_id, version);
CREATE INDEX idx_standard_answer_versions ON standard_answer_versions(standard_answer_id, version);
CREATE INDEX idx_key_point_versions ON key_point_versions(key_point_id, version);
CREATE INDEX idx_option_versions ON option_versions(option_id, version);

-- 添加存储过程：更新标准问题并保存历史版本
DELIMITER //
CREATE PROCEDURE update_standard_question(
    IN p_question_id INT,
    IN p_new_question TEXT,
    IN p_category_id INT,
    IN p_question_type ENUM('single_choice', 'multiple_choice', 'simple_fact', 'subjective'),
    IN p_difficulty ENUM('easy', 'medium', 'hard'),
    IN p_user_id INT,
    IN p_change_reason TEXT
)
BEGIN
    DECLARE current_version INT;
    
    -- 获取当前版本
    SELECT version INTO current_version FROM standard_questions 
    WHERE standard_question_id = p_question_id;
    
    -- 保存当前版本到历史表
    INSERT INTO standard_question_versions (
        standard_question_id, question, category_id, question_type, 
        difficulty, version, changed_by, change_reason
    )
    SELECT 
        standard_question_id, question, category_id, question_type, 
        difficulty, version, p_user_id, p_change_reason
    FROM standard_questions
    WHERE standard_question_id = p_question_id;
    
    -- 更新标准问题表中的数据并递增版本号
    UPDATE standard_questions SET
        question = p_new_question,
        category_id = p_category_id,
        question_type = p_question_type,
        difficulty = p_difficulty,
        updated_at = CURRENT_TIMESTAMP,
        version = current_version + 1
    WHERE standard_question_id = p_question_id;
END //
DELIMITER ;

-- 添加存储过程：更新标准答案并保存历史版本
DELIMITER //
CREATE PROCEDURE update_standard_answer(
    IN p_answer_id INT,
    IN p_new_answer TEXT,
    IN p_user_id INT,
    IN p_change_reason TEXT
)
BEGIN
    DECLARE current_version INT;
    
    -- 获取当前版本
    SELECT version INTO current_version FROM standard_answers 
    WHERE standard_answer_id = p_answer_id;
    
    -- 保存当前版本到历史表
    INSERT INTO standard_answer_versions (
        standard_answer_id, answer, version, changed_by, change_reason
    )
    SELECT 
        standard_answer_id, answer, version, p_user_id, p_change_reason
    FROM standard_answers
    WHERE standard_answer_id = p_answer_id;
    
    -- 更新标准答案表中的数据并递增版本号
    UPDATE standard_answers SET
        answer = p_new_answer,
        updated_at = CURRENT_TIMESTAMP,
        version = current_version + 1
    WHERE standard_answer_id = p_answer_id;
END //
DELIMITER ;

-- 添加存储过程：更新关键点并保存历史版本
DELIMITER //
CREATE PROCEDURE update_key_point(
    IN p_key_point_id INT,
    IN p_new_point_text TEXT,
    IN p_point_weight DECIMAL(5,2),
    IN p_point_type ENUM('required', 'bonus', 'penalty'),
    IN p_example_text TEXT,
    IN p_user_id INT,
    IN p_change_reason TEXT
)
BEGIN
    DECLARE current_version INT;
    
    -- 获取当前版本
    SELECT version INTO current_version FROM answer_key_points 
    WHERE key_point_id = p_key_point_id;
    
    -- 保存当前版本到历史表
    INSERT INTO key_point_versions (
        key_point_id, point_text, point_weight, point_type, 
        example_text, version, changed_by, change_reason
    )
    SELECT 
        key_point_id, point_text, point_weight, point_type, 
        example_text, version, p_user_id, p_change_reason
    FROM answer_key_points
    WHERE key_point_id = p_key_point_id;
    
    -- 更新关键点表中的数据并递增版本号
    UPDATE answer_key_points SET
        point_text = p_new_point_text,
        point_weight = p_point_weight,
        point_type = p_point_type,
        example_text = p_example_text,
        updated_at = CURRENT_TIMESTAMP,
        version = current_version + 1
    WHERE key_point_id = p_key_point_id;
END //
DELIMITER ; 